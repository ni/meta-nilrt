#!/bin/bash
source "$(dirname "$0")"/common.cfg

# Start background disk I/O load
echo "Starting fio load..."
LOAD_CONT=$(docker run -d --privileged -v ${PWD}:/ptests -t parallel-container \
    bash run_fio.sh)

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
       bash "${TEST_DIR}/run-cyclictest" "fio_containerized"
RESULT=$?

# Clean up the background container
docker exec ${LOAD_CONT} bash -c "killall -INT fio > /dev/null 2>&1"

exit ${RESULT}
