# Onboard uses unicode glyphs in its key_defs.xml file, which means
# we need a font that has those glyphs present.
RDEPENDS_${PN}_append += "ttf-dejavu-sans"
