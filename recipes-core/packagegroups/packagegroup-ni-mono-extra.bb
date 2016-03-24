SUMMARY = "Mono support packages"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS_${PN} = "\
	mono mono-helloworld fsharp gtk-sharp mono-basic \
	mozroot-certdata \
	"
