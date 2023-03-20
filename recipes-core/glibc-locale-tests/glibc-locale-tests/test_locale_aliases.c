#include <locale.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <wchar.h>

// L1 Locale (Equivalent to ISO-8859-1 charmap)
// In C, you cannot use universal notation (\uXXXX) for characters in the basic character set (u0000-u009F),
// so use normal hex here instead.
const wchar_t* latin_unicode_string = L"\x4B\u00F6\x6E\x65\x6E\x20\u00D8\u00DA\x45"; // Könen ØÚE
const char* latin_multibyte_file = "L1.txt";

// CP932 Locale (Equivalent to WINDOWS-31J charmap)
const wchar_t* japanese_unicode_string = L"\u540D\u524D\u306F\u306A\u3093\u3067\u3059\u304B"; // 名前はなんですか
const char* japanese_multibyte_file = "CP932.txt";

// CP936 Locale (Equivalent to GBK charmap)
const wchar_t* chinese_unicode_string = L"\u4F60\u53EB\u4EC0\u4E48\u540D\u5B57"; // 你叫什么名字
const char* chinese_multibyte_file = "CP936.txt";

int set_locale(char* locale_alias_name) {
    setlocale(LC_ALL, locale_alias_name);
    return strcmp(locale_alias_name, setlocale(LC_ALL, NULL));
}

int convert_ws_to_mbs(const wchar_t* wide_char_string, char** actual_hex_string) {
    int len = wcstombs(NULL, wide_char_string, 0);
    if (len > 0) {
        *actual_hex_string = malloc((len + 1) * sizeof(char));
        return wcstombs(*actual_hex_string, wide_char_string, len + 1);
    }
    return len;
}

int convert_mbs_to_ws(const char* multibyte_string, wchar_t** wide_string) {
    int len = mbstowcs(NULL, multibyte_string, 0);
    if (len > 0) {
        *wide_string = malloc((len + 1) * sizeof(wchar_t));
        return mbstowcs(*wide_string, multibyte_string, len + 1);
    }
    return len;
}

int test_locale_conversion(char* locale_alias_name, const char* multibyte_file, const wchar_t* unicode_string) {
    FILE* fp;
    char multibyte_string[50];
    wchar_t* converted_unicode_string = NULL;
    char* converted_multibyte_string = NULL;
    int return_code = 0;

    if (set_locale(locale_alias_name)) {
        printf("Could not set locale to %s.\n", locale_alias_name);
        return -1;
    }

    fp = fopen(multibyte_file, "r");
    if (fp == NULL) {
        printf("Unable to open file %s\n", multibyte_file);
        return -1;
    }
    fgets(multibyte_string, 50, fp);
    fclose(fp);

    if (convert_mbs_to_ws(multibyte_string, &converted_unicode_string) <= 0) {
        printf("Unable to convert mbs to wcs: %s\n", multibyte_string);
        return_code = -1;
    } else if (wcscmp(unicode_string, converted_unicode_string)) {
        printf("Converted wcs %ls did not match expected string %ls\n", converted_unicode_string, unicode_string);
        return_code = -1;
    }

    if (convert_ws_to_mbs(unicode_string, &converted_multibyte_string) <= 0) {
        printf("Unable to convert wcs to mbs: %ls\n", unicode_string);
        return_code = -1;
    } else if (strcmp(multibyte_string, converted_multibyte_string)) {
        printf("Converted mbs %s did not match expected string %s\n", converted_multibyte_string, multibyte_string);
        return_code = -1;
    }

    free(converted_unicode_string);
    free(converted_multibyte_string);
    return return_code;
}

int main() {
    int return_code = EXIT_SUCCESS;

    // L1
    if (test_locale_conversion("L1", latin_multibyte_file, latin_unicode_string)) {
        printf("ERROR: Failed to convert strings with locale alias L1.\n");
        return_code = EXIT_FAILURE;
    }

    // CP932
    if (test_locale_conversion("CP932", japanese_multibyte_file, japanese_unicode_string)) {
        printf("ERROR: Failed to convert strings with locale alias CP932.\n");
        return_code = EXIT_FAILURE;
    }

    // CP936
    if (test_locale_conversion("CP936", chinese_multibyte_file, chinese_unicode_string)) {
        printf("ERROR: Failed to convert strings with locale alias CP936.\n");
        return_code = EXIT_FAILURE;
    }

    return return_code;
}