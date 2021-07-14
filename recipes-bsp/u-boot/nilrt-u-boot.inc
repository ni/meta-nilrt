UBOOT_BRANCH ?= "nizynq/21.0/v2017.03"
UBOOT_MACHINE ?= "ni_elvisiii_defconfig"

LIC_FILES_CHKSUM = "\
	file://Licenses/bsd-2-clause.txt;md5=6a31f076f5773aabd8ff86191ad6fdd5; \
	file://Licenses/bsd-3-clause.txt;md5=4a1190eac56a9db675d58ebe86eaf50c; \
	file://Licenses/eCos-2.0.txt;md5=b338cb12196b5175acd3aa63b0a0805c; \
	file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263; \
	file://Licenses/ibm-pibs.txt;md5=c49502a55e35e0a8a1dc271d944d6dba; \
	file://Licenses/isc.txt;md5=ec65f921308235311f34b79d844587eb; \
	file://Licenses/lgpl-2.0.txt;md5=5f30f0716dfdd0d91eb439ebec522ec2; \
	file://Licenses/lgpl-2.1.txt;md5=4fbd65380cdd255951079008b364516c; \
	file://Licenses/OFL.txt;md5=33b7c08863f9722a012141a11fb0e40a; \
	file://Licenses/x11.txt;md5=b46f176c847b8742db02126fb8af92e2; \
"

SRC_URI = "${NILRT_GIT}/u-boot.git;protocol=git;branch=${UBOOT_BRANCH}"
SRCREV = "653fc09e01ff78e08f9683e1585f4fac05f289d9"
