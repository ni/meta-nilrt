PACKAGE := ni-device-encryption

DESCRIPTION := Tools for encrypting NI devices.
VERSION ?= dev


# SOURCE FILES

srcdir = .
srcdatadir = $(srcdir)/share
srcbindir = $(srcdir)/bin

SRC = \
	$(wildcard $(srcbindir)/*) \
	$(wildcard $(srcdatadir)/*) \
	$(srcdatadir)/pkg.sh \
	init/ni-cryptdisks.sh


# INSTALL DIRS

prefix ?= /usr/local

datadir ?= $(prefix)/share
libdir ?= $(prefix)/lib
sysconfdir ?= /etc

pkgdatadir = $(datadir)/$(PACKAGE)
pkglibdir = $(libdir)/$(PACKAGE)

.DEFAULT_GOAL := all


# ==============================================================================
# REAL TARGETS
# ==============================================================================

$(PACKAGE).pc :
	@{ \
		echo "Name: $(PACKAGE)"; \
		echo "Description: $(DESCRIPTION)"; \
		echo "Version: $(VERSION)"; \
		\
		echo "datadir=$(datadir)"; \
		echo "libdir=$(libdir)"; \
		echo "prefix=$(prefix)"; \
		\
		echo "pkgdatadir=$(pkgdatadir)"; \
		echo "pkglibdir=$(pkglibdir)"; \
	} >"$@"


$(srcdatadir)/pkg.sh : $(srcdatadir)/pkg.sh.in
	sed \
		-e 's/@PACKAGE@/$(PACKAGE)/g' \
		-e 's/@VERSION@/$(VERSION)/g' \
		$< >$@


init/ni-cryptdisks.sh : init/ni-cryptdisks.sh.in
	sed \
		-e 's%@pkglibdir@%$(pkglibdir)%g' \
		$< >$@


# ==============================================================================
# PHONY TARGETS
# ==============================================================================

all : $(SRC) $(PACKAGE).pc
.PHONY : all


clean :
	rm -f $(PACKAGE).pc
	rm -f $(srcdatadir)/pkg.sh
	rm -f init/ni-cryptdisks.sh
.PHONY : clean


install : all makeinstalldirs
	install -m 0755 $(srcbindir)/* $(DESTDIR)$(pkglibdir)/bin/
	install -m 0644 $(srcdatadir)/*.sh $(DESTDIR)$(pkglibdir)/share/

	# pkg-config
	install -m 0644 $(PACKAGE).pc $(DESTDIR)$(datadir)/pkgconfig/$(PACKAGE).pc
.PHONY : install


makeinstalldirs :
	mkdir -p $(DESTDIR)$(pkglibdir)
	install -d $(DESTDIR)$(pkglibdir)/bin
	install -d $(DESTDIR)$(pkglibdir)/share
	install -d $(DESTDIR)$(datadir)/pkgconfig
.PHONY : makeinstalldirs


uninstall :
	rm -rf $(DESTDIR)$(pkglibdir)
	rm -f $(DESTDIR)$(datadir)/pkgconfig/$(PACKAGE).pc
.PHONY : uninstall
