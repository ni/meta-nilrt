
# NILRT ARM images use linux kernel 4.14 - which does not support perf
# scripting using python3. Disable `scripting` PACKAGECONFIG in that case, to
# keep python3 out of the perf config.
PACKAGECONFIG_remove_xilinx-zynq += "scripting"
