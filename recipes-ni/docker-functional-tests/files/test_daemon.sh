#!/bin/sh

# Hack: Docker-CE must be restarted before containers can build. This is the first ran test
/etc/init.d/docker.init restart

echo "Running docker hello-world..."
if ! docker run hello-world 2>&1; then
    echo "ERROR: Couldn't execute \`docker run hello-world\`"
    exit 1
fi

exit 0

