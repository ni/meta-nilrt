#!/bin/bash
source "$(dirname "$0")"/common.cfg

CPUS=`nproc --all`
if [ $CPUS -lt 2 ]; then
	echo "ERROR: the containerized hackbench test requires a system with at least 2 CPUs"
	echo "SKIP: test_kernel_hackbench_containerized"
	exit 77
fi

# Start background scheduler load
LOAD_CONT=$(docker run -d --privileged --network=host \
		   -v ${LOG_DIR}:${LOG_DIR} \
		   -v ${TEST_DIR}:${TEST_DIR} \
		   -v /home/admin:/home/admin \
		   -t parallel-container)
docker exec -d $LOAD_CONT "$TEST_DIR/hackbench-load" "start"

# Run cyclictest containers on the last 2 CPUs on the system

"$TEST_DIR/run-docker-cyclictest" "hackbench_containerized_container_1" $((CPUS - 1)) &
PID1=$!

"$TEST_DIR/run-docker-cyclictest" "hackbench_containerized_container_2" $((CPUS - 2))
CYCLICTEST_RESULT_2=$?

# Wait for first cyclictest to finish
wait $PID1
CYCLICTEST_RESULT_1=$?

# Aggregate results from both cyclictests
CYCLICTEST_RESULT=$(( $CYCLICTEST_RESULT_1 + $CYCLICTEST_RESULT_2 ))

# Clean up the background container
docker exec $LOAD_CONT "$TEST_DIR/hackbench-load" "stop"
docker stop $LOAD_CONT

exit $CYCLICTEST_RESULT
