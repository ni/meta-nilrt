#!/bin/bash
source "$(dirname "$0")"/common.cfg

# Start background network load
LOAD_CONT=$(docker run -d --privileged --network=host \
		   -v ${LOG_DIR}:${LOG_DIR} \
		   -v ${TEST_DIR}:${TEST_DIR} \
		   -v /home/admin:/home/admin \
		   -t parallel-container)
docker exec $LOAD_CONT "$TEST_DIR/iperf-load" "check" || exit $?
docker exec -d $LOAD_CONT "$TEST_DIR/iperf-load" "start"

# Run cyclictest
"$TEST_DIR/run-docker-cyclictest" "iperf_containerized"

# Clean up the background container
docker exec $LOAD_CONT "$TEST_DIR/iperf-load" "stop"
docker stop $LOAD_CONT

exit $CYCLICTEST_RESULT
