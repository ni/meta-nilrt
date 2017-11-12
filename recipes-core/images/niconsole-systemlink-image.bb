# Start with the niconsole inage as a base
require niconsole-image.bb

DESCRIPTION = "SystemLink Runmode image for ethernet based, console only, NI Linux Realtime targets."

IMAGE_INSTALL += "\
	salt-minion \
	"
