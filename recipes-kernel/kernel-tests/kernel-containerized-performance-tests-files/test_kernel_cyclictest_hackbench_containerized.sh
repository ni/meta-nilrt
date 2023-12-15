#!/bin/bash
source "$(dirname "$0")"/common.cfg

# Start background scheduler load
echo "Starting hackbench load..."
LOAD_CONT=$(docker run -d --privileged --network=host \
		   -v ${LOG_DIR}:${LOG_DIR} \
		   -v ${TEST_DIR}:${TEST_DIR} \
		   -v /home/admin:/home/admin \
		   -t parallel-container)
docker exec -d ${LOAD_CONT} "${TEST_DIR}/hackbench-load" "start"

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
       "${TEST_DIR}/run-cyclictest" "hackbench_containerized"
RESULT=$?

# Clean up the background container
docker exec ${LOAD_CONT} "${TEST_DIR}/hackbench-load" "stop"
docker stop ${LOAD_CONT}

exit ${RESULT}

