meta-nilrt
=====

This layer provides support for the NI Linux Real-Time distribution. The NI Linux Real-Time distribution supplies a real-time operating system that runs on National Instruments RT targets, such as NI CompactRIO, and provides the flexibility and customization benefits of a Linux system.

This layer depends on:

URI: git://github.com/ni/openembedded-core.git
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-oe.git
layers: meta-oe, meta-gnome, meta-xfce, meta-networking, meta-webserver, meta-filesystems,
meta-perl, meta-python
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-cloud-services.git
layers: meta-openstack
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-selinux.git
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-virtualization.git
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-mono.git
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-security.git
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-sdr.git
branch: nilrt/comms-2.0/fido
revision: HEAD

URI: git://github.com/ni/meta-java.git
branch: nilrt/comms-2.0/fido
revision: HEAD

About NI Linux Real-Time
------------------------
To learn about NI Linux Real-Time, visit the [NI Linux Real-Time Portal].

Building the Layer
-----------

To build the meta-nilrt layer, follow the instructions available on [Github].

Contact
----
Email [licensing@ni.com]

[NI Linux Real-Time Portal]:http://www.ni.com/white-paper/14627/en/
[Github]:https://www.github.com/ni/nilrt
[licensing@ni.com]:mailto:licensing@ni.com
