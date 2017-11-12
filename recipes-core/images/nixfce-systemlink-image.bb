# Start with the nixfce image as a base
require nixfce-image.bb

DESCRIPTION = "SystemLink runmode image for ethernet-based NI Linux Real-Time targets running XFCE."

IMAGE_INSTALL += "\
	salt-minion \
	"
