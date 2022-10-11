# Some recipes - notably ffmpeg - are marked as containing "commercial"
# licenses. In order for NILRT to distribute these recipes, you must inherit
# this bbclass and implement a sanity check to verify that the resulting
# package doesn't violate the recipe's redistributability requirements.

LICENSE_FLAGS_WHITELIST += "commercial_${BPN}"


do_sanity_check_commercial() {
	# Fail by default. Overwrite this function to implement a proper sanity
	# check for the recipe.
	bbfatal_log "do_sanity_check_commercial() is not defined for ${PN}."
}

addtask do_sanity_check_commercial after do_package before do_build
