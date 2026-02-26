# List of returnable errors
declare -A EXITCODES=(
	[OK]=0                      # Success
	[UNSPECIFIED]=1             # No reason specified
	[UNKNOWN_ERROR]=2           # Unexpected exit code encountered
	[INVALID_ARGUMENT]=3        # An invalid combination of arguments was specified
	[INVALID_FSTYPE]=4          # An invalid fstype was specified
	[ILLTIMED]=5                # Called at an inappropriate time
	[INVALID_ARCH]=6            # Running an unsupported processor architecture
	[NO_KEY_BACKUP_DEVICE]=7    # Partition to store backup key could not be found; perhaps it's mislabeled or not plugged in
	[IMPORT_ERROR]=8            # Error importing a script library
	[BAD_ENVIRONMENT]=9         # The script was invoked in an unexpected environment, such as with missing dependencies or on an unsupported OS version
)
export EXITCODES
