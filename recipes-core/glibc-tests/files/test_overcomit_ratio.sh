#!/bin/bash

overcomit_memory=$(cat /proc/sys/vm/overcommit_ratio)
exit $([ $overcomit_memory -le 100 ])
