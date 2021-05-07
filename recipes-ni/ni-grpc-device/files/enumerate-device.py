#
# This example lists out devices connected to the server machine
# by establishing communication with it over gRPC.
#
# Getting Started:
#
# To run this example, install the "NI System Configuration API" on the server machine.
# Link to the download page: https://www.ni.com/en-in/support/downloads/drivers/download.system-configuration.html
#
# For instructions on how to use protoc to generate gRPC client interfaces, see our "Creating a gRPC Client" wiki page.
# Link: https://github.com/ni/grpc-device/wiki/Creating-a-gRPC-Client
#
# Running from command line:
#
# Server machine's IP address and port number can be passed as separate command line arguments.
#   > python enumerate-device.py <server_address> <port_number>
# If they are not passed in as command line arguments, then by default the server address will be "localhost:31763"

import grpc
import sys
import session_pb2 as session_types
import session_pb2_grpc as grpc_session

server_address = "localhost"
server_port = "31763"

# Helper to print the devices 
def print_devices(devices) :
    if not devices:
        print("No devices are connected.")
        return
    print("\n-----------------------------------------------------------------------------------------------------\n")
    print("  List of devices connected to the server: \n")
    print("-----------------------------------------------------------------------------------------------------\n")
    for device in devices :
        print("    {}".format(device.name))
        print("        Model: {}".format(device.model))
        print("        Vendor: {}".format(device.vendor))
        print("        Serial Number: {} \n".format(device.serial_number))

# Read in cmd args
if len(sys.argv) >= 2:
    server_address = sys.argv[1]
if len(sys.argv) >= 3:
    server_port = sys.argv[2]

# Create communication with the server using gRPC APIs.
channel = grpc.insecure_channel("{}:{}".format(server_address, server_port))
client = grpc_session.SessionUtilitiesStub(channel)

try :
    # EnumerateDevices API gives a list of devices (simulated and physical) connected to the server machine.
    enumerate_devices_response = client.EnumerateDevices(session_types.EnumerateDevicesRequest())

    # Display devices connected to the server machine.
    print_devices(enumerate_devices_response.devices)     

# If EnumerateDevices API throws an exception, print the error message.
except grpc.RpcError as rpc_error:
    error_message = rpc_error.details()
    if rpc_error.code() == grpc.StatusCode.UNAVAILABLE :
        error_message = "Failed to connect to the server on {}".format(server_address)
    print("{}".format(error_message))