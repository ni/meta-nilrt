
# NILRT ARM images use linux kernel 4.14 - which does not support perf
# scripting using python3. Disable `scripting` PACKAGECONFIG in that case, to
# keep python3 out of the perf config.
PACKAGECONFIG:remove:xilinx-zynq = "scripting"

# Older xilinx-zynq kernels do not ship the arm64-only perf source fragments
# listed by the upstream recipe, so avoid probing for them during configure.
PERF_SRC:remove:xilinx-zynq = "arch/arm64/tools include/uapi/asm-generic/Kbuild"
