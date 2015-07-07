SUMMARY = "Mono support packages"
LICENSE = "MIT"
PR = "r1"

inherit packagegroup

RDEPENDS_${PN} = "\
	mono mono-helloworld fsharp gtk-sharp mono-basic \
	mozroot-certdata \
	"
