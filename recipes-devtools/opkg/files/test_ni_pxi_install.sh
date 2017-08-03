#!/bin/bash

opkg update > /dev/null

all_pkg="lvrt ni-sysmgmt-salt-minion-support ni-pxisystemplatform"
for pkg in $all_pkg; do
	installed_pkg=$(opkg list-installed $pkg) #check if the package is installed
	if [[ "x$installed_pkg" == "x" ]];then #if package not installed
		opkg install $pkg > /dev/null
		if [[ $? ]];then
			installed_pkg=$(opkg list-installed $pkg) #the package is installed
			installed_files=$(opkg files $pkg) #there are files installed
			if [[ "x$installed_pkg" == "x" || "x$installed_files" == "x" ]];then
				exit 1
			fi
		fi
	fi
done

