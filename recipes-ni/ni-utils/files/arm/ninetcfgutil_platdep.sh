# Copyright (c) 2013 National Instruments Corporation. All rights reserved.

impl_pulldefault_primary () {
	# For ipresetcmd: Just fw_setenv every variable, then nirtcfg --migrate.
	# This avoids having to invert the mapping from ini token to u-boot
	# variable defined in
	# penguin://NIComponents/Embedded/TargetConfiguration/osLevelNetAndSysCfg/
	# trunk/1.1/source/ni/netAndSysCfg/rtcfg/configuration/tUbootVariable.cpp.
	local -r IPRESETCMD_STR="$(fw_printenv -n ipresetcmd)"
	IFS=";" read -ra IPRESETCMD_ARR <<< "$IPRESETCMD_STR"
	local -a SETTINGS_OUT
	for l in "${IPRESETCMD_ARR[@]}"; do
		echo_verbose 2 "$l"
		if [[ $l =~ ^\ *(if|for|while|until)\  ]]; then
			die UNKNOWN "Control flow unsupported in ipresetcmd:" "$l"
		elif [[ $l =~ ^\ *setenv\ +([^\ ]*)\ *(.*) || \
			$l =~ ^\ *([^\ =])\ *=\ *(.*) ]]
		then
			declare NAME="${BASH_REMATCH[1]}" VALUE="${BASH_REMATCH[2]}"
			echo_verbose 2 "    assignment"

			# We don't care about these
			if [[ $NAME == silent || $NAME == savesilent ]]; then
				echo_verbose 2 "    skipping"
				continue
			fi

			# Resolve variable references to u-boot variables
			while [[ $VALUE =~ $'$'([a-zA-Z0-9_]+) || \
				$VALUE =~ $'${'([^}]*)} ]]; do
				local VAR_REF="${BASH_REMATCH[1]}"
				local VAR_VAL="$(fw_printenv -n $VAR_REF 2>/dev/null || true)"
				echo_verbose 2 "    Reference: \${$VAR_REF}=$VAR_VAL"
				VALUE="${VALUE/${BASH_REMATCH[0]}/$VAR_VAL}"
				echo_verbose 2 "    ... value expanded to $VALUE"
			done
			if [[ -z $VALUE ]]; then
				echo_verbose 2 "    (erasure)"
				SETTINGS_OUT+=("$NAME")
			else
				SETTINGS_OUT+=("$NAME"$'\t'"$VALUE")
			fi
		else
			echo_verbose 2 "    skipping"
		fi
	done

	if (( $VERBOSE >= 1 )); then
		echo_verbose 1 "U-Boot settings to apply:"
		printf '\t%s\n' "${SETTINGS_OUT[@]}" >&2
	fi

	( IFS=$'\n\t '; run_cmd fw_setenv -s - <<< "${SETTINGS_OUT[*]}" )
	run_cmd $NIRTCFG --migrate
}


