SUMMARY = "Core feed packages which are required and maintained by NILRT community members."
LICENSE = "MIT"
# All entries within this packagegroup must include a community maintainer
# contact.
# Dependency: <the community project which has this dependency>
# Contact: <a DEVELOPER contact who can speak to the requirement>
# RDEPENDS:${PN} += ""

inherit packagegroup

# Dependency: LQ-Bindings <https://github.com/JKSH/LQ-Bindings>
# Contact: Sze Howe Koh <szehowe.koh@gmail.com>
RDEPENDS:${PN} += "libxkbcommon"
