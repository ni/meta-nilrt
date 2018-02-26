#!/bin/bash

nirtcfg_tempdir=$(mktemp -d)
export nirtcfg_tempdir

nirtcfg_path=$(which nirtcfg)
export nirtcfg_path
