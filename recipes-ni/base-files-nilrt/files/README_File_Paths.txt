File System and Directory Structure
================================================

This RT target's file system follows conventions established for
UNIX-style operating systems.

Other RT targets follow Microsoft Windows-style conventions. In order
to facilitate the porting of applications from those targets,
this target supports the Windows-style "/C" home directory.

Various LabVIEW Real-Time system files which would be accessible from
"C:" (or "/C") on other RT targets are found in different locations
on this target.

UNIX-style file systems support the concept of a symbolic link which
allows access to a file via an alternate file path.  For example, it
is possible to link "/C/ni-rt/system", where dynamic libraries are
deployed on other LabVIEW RT targets, to "/usr/local/lib", where they
are stored on this target, if the application requires this.  An
example of this is installed: /C/ni-rt/startup is a symbolic link to
/home/lvuser/natinst/bin.

For more information about the file system and directory structure on
your RT target, visit ni.com/info and enter Info Code: RT_Paths



Trademarks
==========

Refer to the NI Trademarks and Logo Guidelines at ni.com/trademarks
for information on National Instruments trademarks. Other product and
company names mentioned herein are trademarks or trade names of their
respective companies.
