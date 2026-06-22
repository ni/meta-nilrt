/*
 * ni-pcr-precalc.c - Pre-calculate an expected TPM PCR value for NILRT
 *                    measured boot
 *
 * Simulates the PCR extensions that GRUB performs during boot by reading
 * boot artifacts in the same order and using the same formula as GRUB's
 * verifier framework. The result is the PCR value the TPM is expected to
 * hold after the next boot, allowing LUKS keys to be resealed beforehand.
 *
 * PCR extend formula (per TPM2 spec):
 *   new_PCR = SHA256(current_PCR[32] || SHA256(file_content))
 * Starting PCR value: 0x00...00 (32 zero bytes, confirmed on NILRT hardware).
 *
 * Artifact list format (one entry per line):
 *   # comment lines and blank lines are ignored
 *   nigrub:<path>    file at <path> relative to --nigrub-mount root
 *   /<path>          absolute filesystem path (use for nibootfs files)
 *   Paths containing * or ? are expanded as glob patterns (sorted).
 *   Missing or non-regular files are skipped (logged to stderr), mirroring
 *   GRUB's "if [ -f ... ]" guards.
 *
 * Copyright (c) 2026 Emerson T&M (NI). LICENSE: MIT
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <errno.h>
#include <glob.h>
#include <sys/stat.h>

#include <openssl/evp.h>

/* ============================================================================
 * CONSTANTS
 * ============================================================================ */

#define PCR_SIZE   32     /* SHA256 output size in bytes */
#define MAX_PATH   4096
#define MAX_LINE   8192


/* ============================================================================
 * PCR SIMULATION STATE
 * ============================================================================ */

/* Simulated PCR value; initialized to all zeros in main() */
static uint8_t g_pcr[PCR_SIZE];


/* ============================================================================
 * CRYPTO HELPERS
 * ============================================================================ */

/* Compute SHA256(in[len]) and store 32 bytes in out[]. Returns 0 on success. */
static int sha256_buf(const uint8_t *in, size_t len, uint8_t out[PCR_SIZE])
{
    EVP_MD_CTX *ctx = EVP_MD_CTX_new();
    unsigned int outlen = PCR_SIZE;
    int ret = -1;

    if (!ctx)
        goto done;
    if (!EVP_DigestInit_ex(ctx, EVP_sha256(), NULL))
        goto done;
    if (!EVP_DigestUpdate(ctx, in, len))
        goto done;
    if (!EVP_DigestFinal_ex(ctx, out, &outlen))
        goto done;
    ret = 0;
done:
    EVP_MD_CTX_free(ctx);
    return ret;
}

/*
 * Extend g_pcr with the measurement of data buffer buf[len]:
 *   event_digest = SHA256(buf)
 *   new_pcr      = SHA256(g_pcr || event_digest)
 *
 * Returns 0 on success.
 */
static int pcr_extend(const uint8_t *buf, size_t len)
{
    uint8_t event_digest[PCR_SIZE];
    uint8_t combined[PCR_SIZE * 2];

    if (sha256_buf(buf, len, event_digest) != 0)
        return -1;

    memcpy(combined,            g_pcr,        PCR_SIZE);
    memcpy(combined + PCR_SIZE, event_digest, PCR_SIZE);

    return sha256_buf(combined, sizeof(combined), g_pcr);
}


/* ============================================================================
 * FILE MEASUREMENT
 * ============================================================================ */

/*
 * Read entire file at path and extend g_pcr.
 * Returns:
 *    0  file measured successfully
 *    1  file missing or not a regular file (skipped, logged to stderr;
 *       mirrors GRUB's "if [ -f ... ]" guard)
 *   -1  I/O or other error (message printed to stderr)
 */
static int measure_file(const char *path)
{
    struct stat st;

    if (stat(path, &st) != 0) {
        if (errno == ENOENT) {
            fprintf(stderr, "ni-pcr-precalc: skipping missing artifact '%s'\n", path);
            return 1;  /* missing: skip */
        }
        fprintf(stderr, "ni-pcr-precalc: stat '%s': %s\n", path, strerror(errno));
        return -1;
    }

    if (!S_ISREG(st.st_mode)) {
        fprintf(stderr, "ni-pcr-precalc: skipping non-regular artifact '%s'\n", path);
        return 1;  /* not a regular file: skip */
    }

    /*
     * st_size is off_t (signed, 64-bit under large-file support). Compare in
     * unsigned space after the < 0 guard: casting SIZE_MAX to off_t would be
     * wrong on LP64 (SIZE_MAX exceeds off_t's max), so widen st_size instead.
     */
    if (st.st_size < 0 || (uintmax_t)st.st_size > SIZE_MAX) {
        fprintf(stderr, "ni-pcr-precalc: artifact '%s' too large to hash in-memory (%jd bytes)\n",
                path, (intmax_t)st.st_size);
        return -1;
    }

    size_t size = (size_t)st.st_size;
    uint8_t *buf = NULL;

    if (size > 0) {
        buf = malloc(size);
        if (!buf) {
            fprintf(stderr, "ni-pcr-precalc: out of memory reading '%s' (%zu bytes)\n",
                    path, size);
            return -1;
        }

        FILE *f = fopen(path, "rb");
        if (!f) {
            fprintf(stderr, "ni-pcr-precalc: fopen '%s': %s\n", path, strerror(errno));
            free(buf);
            return -1;
        }

        size_t nr = fread(buf, 1, size, f);
        fclose(f);

        if (nr != size) {
            fprintf(stderr, "ni-pcr-precalc: short read on '%s' (%zu/%zu bytes)\n",
                    path, nr, size);
            free(buf);
            return -1;
        }
    }

    /* Hash the content (empty file: hash of zero bytes) */
    int ret = pcr_extend(buf ? buf : (const uint8_t *)"", size);
    free(buf);

    if (ret != 0) {
        fprintf(stderr, "ni-pcr-precalc: PCR extend failed for '%s'\n", path);
        return -1;
    }
    return 0;
}

/* Sort comparator for glob path arrays */
static int path_strcmp(const void *a, const void *b)
{
    return strcmp(*(const char *const *)a, *(const char *const *)b);
}

/*
 * Expand glob pattern and measure each matching file in sorted (alphabetical)
 * order. A pattern that matches nothing is skipped (logged to stderr); a real
 * glob error (out of memory, read error) is fatal.
 * Returns 0 on success, -1 on error.
 */
static int measure_glob(const char *pattern)
{
    glob_t g;

    int rc = glob(pattern, GLOB_NOSORT, NULL, &g);
    if (rc == GLOB_NOMATCH) {
        fprintf(stderr, "ni-pcr-precalc: skipping unmatched glob '%s'\n", pattern);
        return 0;
    }
    if (rc != 0) {
        fprintf(stderr, "ni-pcr-precalc: glob('%s') failed (error %d)\n", pattern, rc);
        return -1;
    }

    /* Sort results alphabetically */
    if (g.gl_pathc > 1)
        qsort(g.gl_pathv, g.gl_pathc, sizeof(char *), path_strcmp);

    int ret = 0;
    for (size_t i = 0; i < g.gl_pathc; i++) {
        int r = measure_file(g.gl_pathv[i]);
        if (r < 0) {
            ret = -1;
            break;
        }
        /* r == 1: file disappeared between glob and open; skip */
    }

    globfree(&g);
    return ret;
}


/* ============================================================================
 * PATH RESOLUTION
 * ============================================================================ */

/*
 * Resolve an artifact list entry to an OS filesystem path.
 *
 * "nigrub:<rel>"  ->  nigrub_mount + "/" + <rel>
 * "/<abs>"        ->  "/<abs>" as-is
 *
 * Returns a malloc'd string that the caller must free. Returns NULL on error.
 */
static char *resolve_path(const char *entry, const char *nigrub_mount)
{
    char *result = malloc(MAX_PATH);
    if (!result)
        return NULL;

    int n;
    if (strncmp(entry, "nigrub:", 7) == 0) {
        n = snprintf(result, MAX_PATH, "%s/%s", nigrub_mount, entry + 7);
    } else if (entry[0] == '/') {
        n = snprintf(result, MAX_PATH, "%s", entry);
    } else {
        /*
         * Artifact list format only permits "nigrub:<rel>" or an absolute
         * "/<path>". Otherwise it is invalid.
         */
        free(result);
        errno = EINVAL;
        return NULL;
    }

    /*
     * snprintf returns the length it would have written (excluding the NUL);
     * a value >= MAX_PATH means the path was truncated. Fail rather than
     * silently hashing the wrong path; a negative value is an encoding error.
     * Set errno so the caller can report the cause (vs. malloc's ENOMEM).
     */
    if (n < 0 || n >= MAX_PATH) {
        free(result);
        errno = ENAMETOOLONG;
        return NULL;
    }

    return result;
}


/* ============================================================================
 * OUTPUT HELPERS
 * ============================================================================ */

static void print_hex(const uint8_t *buf, size_t len)
{
    for (size_t i = 0; i < len; i++)
        printf("%02x", buf[i]);
    printf("\n");
}

static int write_raw(const char *path, const uint8_t *buf, size_t len)
{
    FILE *f = fopen(path, "wb");
    if (!f) {
        fprintf(stderr, "ni-pcr-precalc: fopen '%s' for write: %s\n",
                path, strerror(errno));
        return -1;
    }
    size_t nw = fwrite(buf, 1, len, f);
    fclose(f);
    if (nw != len) {
        fprintf(stderr, "ni-pcr-precalc: short write to '%s'\n", path);
        return -1;
    }
    return 0;
}


/* ============================================================================
 * MAIN
 * ============================================================================ */

static void usage(const char *prog, FILE *out)
{
    fprintf(out,
        "Usage: %s --artifact-list <path> [OPTIONS]\n"
        "\n"
        "Pre-calculate an expected TPM PCR value for NILRT measured boot.\n"
        "Outputs the PCR value as 64 hex characters on stdout.\n"
        "\n"
        "Required:\n"
        "  --artifact-list <path>        File listing boot artifacts to measure,\n"
        "                                in measurement order\n"
        "\n"
        "Options:\n"
        "  --nigrub-mount <path>         Mount point for nigrub partition\n"
        "                                Default: /mnt/nigrub\n"
        "  --output-pcr <file>           Write raw 32-byte PCR value to file\n"
        "  --pcr7 <file>                 Raw 32-byte PCR 7 input (from tpm2_pcrread)\n"
        "  --output-pcr-digest <file>    Write raw 64-byte PCR7||PCR to file\n"
        "                                (for Clevis pcr_digest; requires --pcr7)\n"
        "  -h, --help                    Show this help and exit\n"
        "\n"
        "Artifact list format:\n"
        "  nigrub:<path>   file on nigrub partition (relative to --nigrub-mount)\n"
        "  /<path>         absolute filesystem path\n"
        "  # ...           comment (ignored)\n"
        "  Paths with * or ? are expanded as sorted globs.\n"
        "  Missing or non-regular files are skipped (logged to stderr).\n",
        prog);
}

int main(int argc, char *argv[])
{
    const char *artifact_list     = NULL;
    const char *nigrub_mount      = "/mnt/nigrub";
    const char *output_pcr        = NULL;
    const char *pcr7_file         = NULL;
    const char *output_pcr_digest = NULL;

    /* --- Argument parsing --- */
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--artifact-list") == 0 && i + 1 < argc) {
            artifact_list = argv[++i];
        } else if (strcmp(argv[i], "--nigrub-mount") == 0 && i + 1 < argc) {
            nigrub_mount = argv[++i];
        } else if (strcmp(argv[i], "--output-pcr") == 0 && i + 1 < argc) {
            output_pcr = argv[++i];
        } else if (strcmp(argv[i], "--pcr7") == 0 && i + 1 < argc) {
            pcr7_file = argv[++i];
        } else if (strcmp(argv[i], "--output-pcr-digest") == 0 && i + 1 < argc) {
            output_pcr_digest = argv[++i];
        } else if (strcmp(argv[i], "-h") == 0 || strcmp(argv[i], "--help") == 0) {
            usage(argv[0], stdout);
            return 0;
        } else {
            fprintf(stderr, "ni-pcr-precalc: unknown argument '%s'\n", argv[i]);
            usage(argv[0], stderr);
            return 2;
        }
    }

    /* --- Validate arguments --- */
    if (!artifact_list) {
        fprintf(stderr, "ni-pcr-precalc: --artifact-list is required\n");
        usage(argv[0], stderr);
        return 2;
    }

    if (output_pcr_digest && !pcr7_file) {
        fprintf(stderr, "ni-pcr-precalc: --output-pcr-digest requires --pcr7\n");
        return 2;
    }

    /* --- Initialize PCR to all zeros --- */
    memset(g_pcr, 0, PCR_SIZE);

    /* --- Process artifact list --- */
    FILE *listf = fopen(artifact_list, "r");
    if (!listf) {
        fprintf(stderr, "ni-pcr-precalc: cannot open artifact list '%s': %s\n",
                artifact_list, strerror(errno));
        return 1;
    }

    char line[MAX_LINE];
    int ret = 0;

    while (fgets(line, sizeof(line), listf)) {
        /* Strip trailing newline/carriage-return */
        size_t len = strlen(line);
        while (len > 0 && (line[len-1] == '\n' || line[len-1] == '\r'))
            line[--len] = '\0';

        /* Skip empty lines and comments */
        if (len == 0 || line[0] == '#')
            continue;

        char *path = resolve_path(line, nigrub_mount);
        if (!path) {
            fprintf(stderr, "ni-pcr-precalc: failed to resolve path for entry '%s': %s\n",
                    line, strerror(errno));
            ret = 1;
            break;
        }

        int r;
        if (strchr(path, '*') || strchr(path, '?'))
            r = measure_glob(path);
        else
            r = measure_file(path);

        free(path);

        if (r < 0) {
            ret = 1;
            break;
        }
        /* r == 1: file missing or non-regular, skipped (already logged) */
    }

    fclose(listf);

    if (ret != 0)
        return ret;

    /* --- Output PCR hex to stdout --- */
    print_hex(g_pcr, PCR_SIZE);

    /* --- Optionally write raw PCR to file --- */
    if (output_pcr) {
        if (write_raw(output_pcr, g_pcr, PCR_SIZE) != 0)
            return 1;
    }

    /* --- Optionally compute pcr_digest = PCR7 || PCR and write to file --- */
    if (output_pcr_digest) {
        uint8_t pcr7[PCR_SIZE];

        FILE *f = fopen(pcr7_file, "rb");
        if (!f) {
            fprintf(stderr, "ni-pcr-precalc: cannot open PCR7 file '%s': %s\n",
                    pcr7_file, strerror(errno));
            return 1;
        }
        size_t nr = fread(pcr7, 1, PCR_SIZE, f);
        fclose(f);

        if (nr != PCR_SIZE) {
            fprintf(stderr,
                    "ni-pcr-precalc: PCR7 file '%s' must be exactly %d bytes "
                    "(got %zu)\n",
                    pcr7_file, PCR_SIZE, nr);
            return 1;
        }

        /*
         * pcr_digest for Clevis is the concatenation of PCR values in index
         * order: PCR7[32] || PCR[32]  (64 bytes total).
         * The caller base64-encodes this before passing to clevis luks bind.
         */
        uint8_t pcr_digest[PCR_SIZE * 2];
        memcpy(pcr_digest,            pcr7,  PCR_SIZE);
        memcpy(pcr_digest + PCR_SIZE, g_pcr, PCR_SIZE);

        if (write_raw(output_pcr_digest, pcr_digest, sizeof(pcr_digest)) != 0)
            return 1;
    }

    return 0;
}
