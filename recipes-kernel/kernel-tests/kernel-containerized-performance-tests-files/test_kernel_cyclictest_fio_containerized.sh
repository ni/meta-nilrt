#!/bin/bash
source "$(dirname "$0")"/common.cfg

# Start background disk I/O load
LOAD_CONT=$(docker run -d --privileged --network=host \
		   -v ${LOG_DIR}:${LOG_DIR} \
		   -v ${TEST_DIR}:${TEST_DIR} \
		   -v /home/admin:/home/admin \
		   -t parallel-container)
docker exec -d $LOAD_CONT "$TEST_DIR/fio-load" "start"

# Run cyclictest
"$TEST_DIR/run-docker-cyclictest" "fio_containerized"

# Clean up the background container
docker exec $LOAD_CONT "$TEST_DIR/fio-load" "stop"
docker stop $LOAD_CONT

exit $CYCLICTEST_RESULT
