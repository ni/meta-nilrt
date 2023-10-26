#!/bin/sh

PTEST_LOCATION=/usr/lib/docker-functional-tests/ptest

if [ "$(docker images -q test_parallel_container:latest)" = "" ]; then
    echo "Building test_parallel_container..."
    DOCKER_BUILDKIT=1 \
        docker build -t test_parallel_container --network=host \
            ${PTEST_LOCATION}/test_parallel_container > /dev/null 2>&1
    if [ "$(docker images -q test_parallel_container:latest)" = "" ]; then
        echo "SKIP: Failed to build test_parallel_container"
        exit 77
    fi
fi

echo "Starting background container..."
BG_CONT=$(docker run -d --privileged --network=host -t test_parallel_container \
    python3 -m http.server)
RESULT=`echo ${BG_CONT} | sed 's/.$//'` # Remove trailing carriage return
echo "Started background container ${BG_CONT}"

echo "Communicating with background container..."
RESULT=$(docker run --privileged --network=host -t test_parallel_container \
    curl http://0.0.0.0:8000/test_file.txt)
RESULT=`echo ${RESULT} | sed 's/.$//'` # Remove trailing carriage return

echo "Stopping background container..."
docker exec ${BG_CONT} bash -c "killall -INT python3 > /dev/null 2>&1"
docker container stop ${BG_CONT}

EXP=$(cat ${PTEST_LOCATION}/test_parallel_container/test_file.txt)
if [ "${RESULT}" != "${EXP}" ]; then
    echo "ERROR: Received \"${RESULT}\" from background container, but expected \"${EXP}\""
    exit 1
fi
exit 0

