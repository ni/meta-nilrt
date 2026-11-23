# Build udisks2 for fwupd without enabling polkit distro-wide, which would pull
# polkit into the BSI via other recipes' PACKAGECONFIG defaults.
REQUIRED_DISTRO_FEATURES:remove = "polkit"
