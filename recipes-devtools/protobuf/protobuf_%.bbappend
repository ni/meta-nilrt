
# Setting the 'compiler' PACKAGECONFIG tells the base recipe to build and package the 'protoc' compiler.
# It is generally useful for NILRT users to have protoc, so that they can compile protobuf projects on-device.
PACKAGECONFIG:append = " compiler"
