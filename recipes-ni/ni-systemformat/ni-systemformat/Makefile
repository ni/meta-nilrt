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
	cp -r $(srcdir)/lib/* $(DESTDIR)$(pkglibdir)
	install -m 0550 $(srcdir)/nisystemformat $(DESTDIR)$(bindir)
.PHONY : install


mkinstalldirs :
	mkdir -p $(DESTDIR)$(bindir)
	mkdir -p $(DESTDIR)$(pkglibdir)
.PHONY : mkinstalldirs


uninstall :
	rm -rf $(DESTDIR)$(pkglibdir)
	rm -f $(DESTDIR)$(bindir)/nisystemformat
.PHONY : uninstall
