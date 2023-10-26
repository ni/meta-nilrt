#!/bin/bash

PTEST_LOCATION=/usr/lib/kernel-containerized-performance-tests/ptest

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
if [ "$(docker images -q parallel-container:latest)" = "" ]; then
    echo "Building parallel-container..."
    DOCKER_BUILDKIT=1 \
        docker build -t parallel-container --network=host ${PTEST_LOCATION}/parallel-container
    if [ "$(docker images -q parallel-container:latest)" = "" ]; then
        echo "Failed to build parallel-container"
        exit 77
    fi
fi

# Start background scheduler load
echo "Starting hackbench load..."
LOAD_CONT=$(docker run -d --privileged -v ${PTEST_LOCATION}:/ptests -t parallel-container \
    bash run_hackbench.sh)

# Run cyclictest
echo "Running cyclictest in docker container..."
RESULT=$(docker run --privileged --network=host \
    -v ${LOG_DIR}:${LOG_DIR} -v ${PTEST_LOCATION}:/ptests -v /home/admin:/home/admin \
    -v /usr/share/fw_printenv:/usr/share/fw_printenv -v /sbin/fw_printenv:/sbin/fw_printenv \
    -v /usr/share/nisysinfo:/usr/share/nisysinfo -v /dev:/dev \
    -t cyclictest-container \
    bash call_run_ct.sh "hackbench_containerized" \
        | tr -d '\r' | tr -d '\n')

# Clean up the background container
docker exec ${LOAD_CONT} \
    bash -c "killall -INT hackbench > /dev/null 2>&1"

echo "Test Result: ${RESULT}"
exit ${RESULT}

