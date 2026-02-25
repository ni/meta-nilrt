# CONSTANTS

PACKAGE = ni-systemformat
VERSION = 1.0.0


# DIRECTORIES
## Source directories

srcdir = src
builddir = build

## Install directories
prefix ?= /usr/local
exec_prefix ?= ${prefix}

libdir ?= $(prefix)/lib
bindir ?= $(prefix)/bin
sysconfdir ?= $(prefix)/etc

pkglibdir = $(libdir)/$(PACKAGE)


.DEFAULT_GOAL := all


# ==============================================================================
# PHONY TARGETS
# ==============================================================================

all :
.PHONY : all


clean :
	rm -rf $(builddir)
.PHONY : clean


install : mkinstalldirs
	install -m 0550 $(srcdir)/nisystemformat $(DESTDIR)$(bindir)
.PHONY : install


mkinstalldirs :
	mkdir -p $(DESTDIR)$(bindir)
.PHONY : mkinstalldirs


uninstall :
	rm -f $(DESTDIR)$(bindir)/nisystemformat
.PHONY : uninstall
