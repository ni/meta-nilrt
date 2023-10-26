FROM ubuntu:latest

# Install rt-tests, sudo, etc
RUN DEBIAN_FRONTEND=noninteractive apt update && apt install -y \
    curl \
    python3

# Scripts to start loads from outside container
ADD --chmod=664 test_file.txt ./test_file.txt

