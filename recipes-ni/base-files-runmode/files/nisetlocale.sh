#!/bin/sh
#
# Copyright (c) 2013 National Instruments
#

nigetlocale()
{
	local NIRTCFG=/usr/local/natinst/bin/nirtcfg
	local NIRT_INI_SECTION=SystemSettings
	local NIRT_INI_SUPPORTED_SECTION="SUPPORTED LOCALES"
	local NIRT_LOCALE_TOKEN=Language
	local NIRT_LOCALE_ENGLISH_TOKEN=english
	local NIRT_LOCALE_DEFAULT="POSIX"

	#If we are a user that cannot run the tool, don't and just return
	if [ ! -x $NIRTCFG ]; then
		echo $NIRT_LOCALE_DEFAULT
		return
	fi

	#In [Supported Locales], for each locale installed we have a token language_name=locale, i.e.
	#english=L1
	#tells MAX to set the system locale to L1 when the user picks English. This is to simplify
	#the scary world of locales. English is our default language, so we check to see if we have 
	#support for it installed.
	niLangDef=`$NIRTCFG --get section="$NIRT_INI_SUPPORTED_SECTION",token="$NIRT_LOCALE_ENGLISH_TOKEN",value="$NIRT_LOCALE_DEFAULT"`
	if [ $? -ne 0 ]; then
		#Who knows what happened if nirtcfg failed, so set the default to something sane
		niLangDef=$NIRT_LOCALE_DEFAULT
	fi
	#Try to get the actual setting. If token is not present or something went wrong,
	#use our sane default value from earlier
	niLang=`$NIRTCFG --get section="$NIRT_INI_SECTION",token="$NIRT_LOCALE_TOKEN",value="$niLangDef"`
	if [ $? -ne 0 ]; then
		echo $niLangDef
		return
	fi
	echo $niLang
}

export LANG=$(nigetlocale)
