#!/bin/bash
source "$(dirname "$0")"/common.cfg

# Run cyclictest
"$TEST_DIR/run-docker-cyclictest" "idle_containerized"

exit $CYCLICTEST_RESULT
