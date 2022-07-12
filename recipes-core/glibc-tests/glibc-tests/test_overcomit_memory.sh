#!/bin/bash

overcomit_memory=$(cat /proc/sys/vm/overcommit_memory)
exit $([ $overcomit_memory -eq 2 ])
