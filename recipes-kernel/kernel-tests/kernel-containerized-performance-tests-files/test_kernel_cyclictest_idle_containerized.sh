#!/bin/bash

PTEST_LOCATION=/usr/lib/kernel-containerized-performance-tests/ptest
LOG_DIR="/var/local/ptest-results/kernel-containerized-performance-tests"

# Build the two containers
if [ "$(docker images -q cyclictest-container:latest)" = "" ]; then
    echo "Building cyclictest-container..."
    DOCKER_BUILDKIT=1 \
        docker build -t cyclictest-container --network=host ${PTEST_LOCATION}/cyclictest-container
    if [ "$(docker images -q cyclictest-container:latest)" = "" ]; then
        echo "Failed to build cyclictest-container"
        exit 77
    fi
fi

# Run cyclictest
echo "Running cyclictest in docker container..."
RESULT=$(docker run --privileged --network=host \
    -v ${LOG_DIR}:${LOG_DIR} -v ${PTEST_LOCATION}:/ptests -v /home/admin:/home/admin \
    -v /usr/share/fw_printenv:/usr/share/fw_printenv -v /sbin/fw_printenv:/sbin/fw_printenv \
    -v /usr/share/nisysinfo:/usr/share/nisysinfo -v /dev:/dev \
    -t cyclictest-container \
    bash call_run_ct.sh "idle_containerized" \
        | tr -d '\r' | tr -d '\n')

# Make sure we print the PASS/FAIL message
cat ${LOG_DIR}/run_cyclictest-idle_containerized.log

exit ${RESULT}

