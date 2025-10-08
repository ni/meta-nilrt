COMPATIBLE_MACHINE = "xilinx-zynq"
PROVIDES += "fw-printenv"
RPROVIDES:${PN}-bin += "fw-printenv"
RDEPENDS:${PN}-bin += "u-boot-env"
