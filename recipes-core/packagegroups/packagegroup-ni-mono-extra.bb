SUMMARY = "Mono support packages"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	fsharp \
	gtk-sharp \
	mono \
	mono-helloworld \
	mozroot-certdata \
"
