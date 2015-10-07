# Copyright (c) 2014 National Instruments Corporation. All rights reserved.

impl_pulldefault_primary ()
{
	PRIMACDEV=$(get_primary_device)

	/usr/local/natinst/bin/nirtcfg --set section=${PRIMACDEV},token=dhcpenabled,value=1
	/usr/local/natinst/bin/nirtcfg --set section=${PRIMACDEV},token=linklocalenabled,value=1
	/usr/local/natinst/bin/nirtcfg --clear section=${PRIMACDEV},token=sdnsip
	/usr/local/natinst/bin/nirtcfg --clear section=${PRIMACDEV},token=sgatewayip
	/usr/local/natinst/bin/nirtcfg --clear section=${PRIMACDEV},token=sipaddr
	/usr/local/natinst/bin/nirtcfg --clear section=${PRIMACDEV},token=snetmask
	/usr/local/natinst/bin/nirtcfg --clear section=${PRIMACDEV},token=mtu
}


