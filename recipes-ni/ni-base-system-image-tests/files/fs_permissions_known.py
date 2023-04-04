import argparse
import os
import sys
import hashlib
import stat
import pwd
import grp
import fs_permissions_shared

# Function to return tree instead of variable since it contains functions inside its structure
def known_permissions_tree():
    return {
        '.': system_dir,
        'boot': {
            '.': system_dir,
            'bootmode': system_file,
            'grub': {
                '.': system_dir,
                '*': system_file,
                'grubenv': permissions(0o664, 'admin', 'ni', FT_REG),
                'grubenv.bak': permissions(0o664, 'admin', 'ni', FT_REG),
                'recoverytool-ni-version': permissions(0o0444, 'admin', 'administrators', FT_REG)
            },
            'runmode': {
                '.': system_dir,
                '*': system_file,
                'bzImage': system_link(f'bzImage-{bash("$(uname -r)")[0]}'),
            }
        },
        'etc': {
            'fstab': permissions(0o0644, 'lvuser', 'ni', FT_REG)
        },
        'home': {
            '.': system_dir,
            '*': system_dir,
            'lvuser': permissions(0o2775, 'lvuser', 'ni', FT_DIR),
            'root': system_link('/home/admin'),
            'webserv': permissions(0o2755, 'webserv', 'ni', FT_DIR)
        },
        'lib': {
            '.': system_dir,
            'modules': {
                '.': system_dir,
                '$(uname -r)': {
                    '.': system_dir,
                    '**': system_hier,
                    'build': {
                        'arch': {
                            'x86': {
                                'tools': {
                                    'relocs': system_file_exec
                                }
                            }
                        },
                        'include': {
                            'dt-bindings': {
                                'clock': {
                                    'qcom,dispcc-sm8150.h': system_link('qcom,dispcc-sm8250.h')
                                },
                                'input': {
                                    'linux-event-codes.h':
                                        system_link('../../uapi/linux/input-event-codes.h')
                                }
                            }
                        },
                        'scripts': {
                            '**/*.sh': system_file_exec,
                            '**/*.pl': system_file_exec,
                            'asn1_compiler': system_file_exec,
                            'atomic': {
                                'atomics.tbl': system_file_exec,
                                'fallbacks': {
                                    '*': system_file_exec
                                }
                            },
                            'basic': {
                                'fixdep': system_file_exec
                            },
                            'bloat-o-meter': system_file_exec,
                            'bpf_doc.py': system_file_exec,
                            'check-sysctl-docs': system_file_exec,
                            'checkkconfigsymbols.py': system_file_exec,
                            'clang-tools': {
                                'gen_compile_commands.py': system_file_exec,
                                'run-clang-tools.py': system_file_exec
                            },
                            'cleanfile': system_file_exec,
                            'cleanpatch': system_file_exec,
                            'coccicheck': system_file_exec,
                            'config': system_file_exec,
                            'decodecode': system_file_exec,
                            'diffconfig': system_file_exec,
                            'documentation-file-ref-check': system_file_exec,
                            'dtc': {
                                'dt_to_config': system_file_exec,
                                'dtx_diff': system_file_exec,
                                'include-prefixes': {
                                    'arc': system_link('../../../arch/arc/boot/dts'),
                                    'arm': system_link('../../../arch/arm/boot/dts'),
                                    'arm64': system_link('../../../arch/arm64/boot/dts'),
                                    'dt-bindings': system_link('../../../include/dt-bindings'),
                                    'h8300': system_link('../../../arch/h8300/boot/dts'),
                                    'microblaze': system_link('../../../arch/microblaze/boot/dts'),
                                    'mips': system_link('../../../arch/mips/boot/dts'),
                                    'nios2': system_link('../../../arch/nios2/boot/dts'),
                                    'openrisc': system_link('../../../arch/openrisc/boot/dts'),
                                    'powerpc': system_link('../../../arch/powerpc/boot/dts'),
                                    'sh': system_link('../../../arch/sh/boot/dts'),
                                    'xtensa': system_link('../../../arch/xtensa/boot/dts'),
                                }
                            },
                            'dummy-tools': {
                                'gcc': system_file_exec,
                                'ld': system_file_exec,
                                'nm': system_link('ld'),
                                'objcopy': system_link('ld')
                            },
                            'extract-cert': system_file_exec,
                            'extract-ikconfig': system_file_exec,
                            'extract-vmlinux': system_file_exec,
                            'faddr2line': system_file_exec,
                            'gcc-ld': system_file_exec,
                            'genksyms': {
                                'genksyms': system_file_exec
                            },
                            'get_dvb_firmware': system_file_exec,
                            'gfp-translate': system_file_exec,
                            'jobserver-exec': system_file_exec,
                            'kallsyms': system_file_exec,
                            'kconfig': {
                                'conf': system_file_exec
                            },
                            'kernel-doc': system_file_exec,
                            'Lindent': system_file_exec,
                            'makelst': system_file_exec,
                            'mkcompile_h': system_file_exec,
                            'mksysmap': system_file_exec,
                            'mod': {
                                'mk_elfconfig': system_file_exec,
                                'modpost': system_file_exec
                            },
                            'objdiff': system_file_exec,
                            'package': {
                                '*': system_file_exec,
                                'snapcraft.template': system_file
                            },
                            'patch-kernel': system_file_exec,
                            'prune-kernel': system_file_exec,
                            'remove-stale-files': system_file_exec,
                            'selinux': {
                                'genheaders': {
                                    'genheaders': system_file_exec,
                                },
                                'mdp': {
                                    'mdp': system_file_exec
                                }
                            },
                            'setlocalversion': system_file_exec,
                            'show_delta': system_file_exec,
                            'sorttable': system_file_exec,
                            'spdxcheck.py': system_file_exec,
                            'spdxcheck-test.sh': system_file,
                            'sphinx-pre-install': system_file_exec,
                            'stackdelta': system_file_exec,
                            'stackusage': system_file_exec,
                            'tracing': {
                                'draw_functrace.py': system_file_exec
                            },
                            'ver_linux': system_file_exec,
                        },
                        'tools': {
                            'lib': {
                                'lockdep': {
                                    'lockdep': system_file_exec,
                                    'run_tests.sh': system_file_exec
                                }
                            },
                            'objtool': {
                                'fixdep': system_file_exec,
                                'objtool': system_file_exec,
                                'sync-check.sh': system_file_exec
                            }
                        }
                    },
                    'source': system_link('build')
                }
            }
        }
    }

# Use bash to resolve strings like path globs and $() expressions.
def bash(path):
    cmd = [
        'bash',
        '-O', 'extglob',
        '-O', 'globstar',
        '-c', f'echo {path}' ]
    return fs_permissions_shared.run_cmd(cmd).strip().split(' ')

# Convert the tree structure into a list of paths and functions to check their permissions
def known_permissions():
    def permissions_list(tree, path, perms):
        for file in tree:
            if type(tree[file]) is dict:
                perms = permissions_list(tree[file], f'{path}/{file}', perms)
            elif file == '.':
                perms[bash(path if path != '' else '/')[0]] = tree[file]
            else:
                subfiles = bash(f'{path}/{file}')
                for subfile in subfiles:
                    perms[subfile] = tree[file]
        return perms
    return permissions_list(known_permissions_tree(), '', {})

#------- Functions for the tree to check file permissions ------

FT_DIR = 0
FT_REG = 1
FT_LNK = 2

# Report errors when a kind of thing is different than expected
def log_mismatch(kind, exp, act, path, logger, md5sum):
    md5sum.update(f'{kind}: {exp} != {act}'.encode('utf-8'))
    logger.log(f'ERROR: Expected {kind} \'{exp}\' but got \'{act}\' for path {path}.')

# Main check for file/directory permissions
def permissions(mode, user, group, file_type):
    def ret(path, stats, logger, md5sum):
        nonlocal mode, user, group, file_type
        if file_type == FT_DIR and not stat.S_ISDIR(stats.st_mode):
            log_mismatch(
                'file type', 'dir', 'reg' if file_type == FT_REG else 'lnk', path, logger, md5sum
            )
            return False
        if file_type == FT_REG and not stat.S_ISREG(stats.st_mode):
            log_mismatch(
                'file type', 'reg', 'dir' if file_type == FT_DIR else 'lnk', path, logger, md5sum
            )
            return False
        if file_type == FT_LNK and not stat.S_ISLNK(stats.st_mode):
            log_mismatch(
                'file type', 'lnk', 'dir' if file_type == FT_DIR else 'reg', path, logger, md5sum
            )
            return False
        if mode != stat.S_IMODE(stats.st_mode):
            log_mismatch('mode', oct(mode), oct(stat.S_IMODE(stats.st_mode)), path, logger, md5sum)
            return False
        if user != pwd.getpwuid(stats.st_uid).pw_name:
            log_mismatch('user', user, pwd.getpwuid(stats.st_uid).pw_name, path, logger, md5sum)
            return False
        if group != grp.getgrgid(stats.st_gid).gr_name:
            log_mismatch('group', group, grp.getgrgid(stats.st_gid).gr_name, path, logger, md5sum)
            return False
        return True
    return ret

# Typical system file
def system_file(path, stats, logger, md5sum):
    return permissions(0o0644, 'admin', 'administrators', FT_REG)(path, stats, logger, md5sum)

# Sys file with executable bit set
def system_file_exec(path, stats, logger, md5sum):
    return permissions(0o0755, 'admin', 'administrators', FT_REG)(path, stats, logger, md5sum)

# Typical system dir
def system_dir(path, stats, logger, md5sum):
    return permissions(0o0755, 'admin', 'administrators', FT_DIR)(path, stats, logger, md5sum)

# Either system file or system dir
def system_hier(path, stats, logger, md5sum):
    is_dir = stat.S_ISDIR(stats.st_mode)
    mode = 0o0755 if is_dir else 0o0644
    file_type = FT_DIR if is_dir else FT_REG
    return permissions(mode, 'admin', 'administrators', file_type)(path, stats, logger, md5sum)

# What would be a typical system file but is actually a link
def system_link(target):
    def ret(path, stats, logger, md5sum):
        nonlocal target
        real_target = os.readlink(path)
        correct_link = target == real_target
        if not correct_link:
            log_mismatch('link', target, real_target, path, logger, md5sum)
        return \
            permissions(0o0777, 'admin', 'administrators', FT_LNK)(path, stats, logger, md5sum) \
            and correct_link
    return ret

# ------ End tree functions ------

def check_known_permissions(logger):
    logger.log('###### Checking expected file permissions against actual for known folders')
    md5sum = hashlib.md5()
    success = True
    perms = known_permissions()
    for path in perms:
        stats = os.lstat(path)
        if not perms[path](path, stats, logger, md5sum):
            success = False
    logger.log('###### Finished checking file permissions')
    return success

def main():
    argparse.ArgumentParser(description = 'Check fs permissions against expectations').parse_args()
    logger = fs_permissions_shared.Logger()
    
    result = check_known_permissions(logger)

    logger.report()
    if result:
        sys.exit(os.EX_OK)
    else:
        sys.exit(os.EX_SOFTWARE)

if __name__ == '__main__':
    main()

