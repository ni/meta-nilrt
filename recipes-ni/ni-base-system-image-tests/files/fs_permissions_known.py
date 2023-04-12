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
                'grub-ni-version': permissions(0o0444, 'admin', 'administrators', FT_REG),
                'recoverytool-ni-version': permissions(0o0444, 'admin', 'administrators', FT_REG)
            },
            'runmode': {
                '.': system_dir,
                '*': system_file,
                'bzImage': system_link(f'bzImage-{bash("$(uname -r)")[0]}'),
            }
        },
        'etc': {
            'fstab': system_file
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
                    '**': system_hier
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
        success = True
        if file_type == FT_DIR and not stat.S_ISDIR(stats.st_mode):
            log_mismatch(
                'file type', 'dir', 'reg' if file_type == FT_REG else 'lnk', path, logger, md5sum
            )
            success = False
        if file_type == FT_REG and not stat.S_ISREG(stats.st_mode):
            log_mismatch(
                'file type', 'reg', 'dir' if file_type == FT_DIR else 'lnk', path, logger, md5sum
            )
            success = False
        if file_type == FT_LNK and not stat.S_ISLNK(stats.st_mode):
            log_mismatch(
                'file type', 'lnk', 'dir' if file_type == FT_DIR else 'reg', path, logger, md5sum
            )
            success = False
        if mode != stat.S_IMODE(stats.st_mode):
            log_mismatch('mode', oct(mode), oct(stat.S_IMODE(stats.st_mode)), path, logger, md5sum)
            success = False
        if user != pwd.getpwuid(stats.st_uid).pw_name:
            log_mismatch('user', user, pwd.getpwuid(stats.st_uid).pw_name, path, logger, md5sum)
            success = False
        if group != grp.getgrgid(stats.st_gid).gr_name:
            log_mismatch('group', group, grp.getgrgid(stats.st_gid).gr_name, path, logger, md5sum)
            success = False
        return success
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

# Only care about ownership and r/w access not link status, directory status, exec bit, etc
def system_hier(path, stats, logger, md5sum):
    is_dir = stat.S_ISDIR(stats.st_mode)
    is_link = stat.S_ISLNK(stats.st_mode)
    mode = 0o0777 if is_link else (
        0o0755 if is_dir or stat.S_IMODE(stats.st_mode) == 0o0755 else 0o0644
    )
    file_type = FT_DIR if is_dir else (FT_LNK if is_link else FT_REG)
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

