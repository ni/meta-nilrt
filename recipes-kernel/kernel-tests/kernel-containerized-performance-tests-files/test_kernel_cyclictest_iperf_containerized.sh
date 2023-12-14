#!/bin/bash
source "$(dirname "$0")"/common.cfg

source /home/admin/.iperf.info
if [ -z "$IPERF_SERVER" ]; then
    echo "Warning: iperf server not configured; skipping iperf based network load test."
    echo "Create/edit /home/admin/.iperf.info file with IPERF_SERVER=<server> and IPERF_PORT=<port> to configure a server to connect to for this test."
    echo "SKIP: test_kernel_cyclictest_iperf_containerized"
    exit 77
fi
if [ ! -z "$IPERF_PORT" ]; then
    iperf3 -c "$IPERF_SERVER" -p "$IPERF_PORT" -t 1 > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "ERROR: iperf server not reachable; skipping iperf based network load test."
        echo "SKIP: test_kernel_cyclictest_iperf_containerized"
        exit 77
    fi
fi

# Start background network load
echo "Starting iperf load..."
LOAD_CONT=$(docker run -d --privileged \
    -v ${PWD}:/ptests -v /home/admin:/home/admin --network=host \
    -t parallel-container bash run_iperf.sh)

# Run cyclictest
echo "Running cyclictest in docker container..."
docker run --privileged --network=host \
       -v ${LOG_DIR}:${LOG_DIR} \
       -v ${TEST_DIR}:${TEST_DIR} \
       -v /home/admin:/home/admin \
       -v /usr/share/fw_printenv:/usr/share/fw_printenv \
       -v /sbin/fw_printenv:/sbin/fw_printenv \
       -v /usr/share/nisysinfo:/usr/share/nisysinfo \
       -v /dev:/dev \
       -t cyclictest-container \
       bash "${TEST_DIR}/run-cyclictest" "iperf_containerized"
RESULT=$?

# Clean up the background container
docker exec ${LOAD_CONT} \
    bash -c "killall -INT iperf3 > /dev/null 2>&1"

exit ${RESULT}

