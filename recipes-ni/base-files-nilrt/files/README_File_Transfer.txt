WebDAV and SFTP via SSH Support
===============================
This RT target supports WebDAV, a protocol which enables you to create,
change, and move documents on your target securely and effectively.
For more information about transferring files using WebDAV, visit
ni.com/info and enter the Info Code: RTFileTransfer

In addition to WebDAV, this target supports SFTP via SSH as an
alternative file transfer mechanism. A third-party client must
be used to enable this connection.



Note on the FTP Server
======================

National Instruments recommends using WebDAV or SFTP via SSH as the
file transfer mechanism for improved security. However, for legacy
applications that cannot be updated to use a more secure protocol, you
can enable the FTP server in Measurement and Automation Explorer (MAX).



File System and Directory Structure
===================================
This RT target uses a UNIX-style file system and directory structure.
To learn about the file system and directory structure, open
README_File_Paths.txt located on your target or visit ni.com/info
and enter the Info Code: RT_Paths



Trademarks
==========

Refer to the NI Trademarks and Logo Guidelines at ni.com/trademarks
for information on National Instruments trademarks. Other product and
company names mentioned herein are trademarks or trade names of their
respective companies.