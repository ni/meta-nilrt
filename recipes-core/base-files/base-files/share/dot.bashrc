# ~/.bashrc: executed by bash(1) for non-login interactive shells.

[ -z "$PS1" ] && return
[ -z "$BASH_VERSION" ] && return

# Always use ls --color -F, even if dircolors isn't installed: busybox ls
# invariably supports it
alias ls='ls --color=auto -F'

if command -v dircolors >/dev/null 2>&1; then
	eval `dircolors -b`
fi

HISTCONTROL=ignoreboth
shopt -s histappend
shopt -s checkjobs
