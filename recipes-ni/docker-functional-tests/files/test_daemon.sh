#!/bin/sh

echo "Running docker hello-world..."
if ! docker run hello-world 2>&1; then
    echo "ERROR: Couldn't execute \`docker run hello-world\`"
    exit 1
fi

echo "PASS: Docker - test daemon"

exit 0

