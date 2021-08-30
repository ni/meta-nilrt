# PXIe-88XX series hardware requires the r600_dri driver for hardware rendering
PACKAGECONFIG_append = "\
	gallium \
	r600 \
"
