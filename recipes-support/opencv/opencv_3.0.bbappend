DEFAULT_PREFERENCE ="0"

SRC_URI_remove = "git://github.com/Itseez/opencv_contrib.git;destsuffix=contrib;name=contrib"

python () {
    d.delVar("SRCREV_contrib")
}

EXTRA_OECMAKE_remove = "-DOPENCV_EXTRA_MODULES_PATH=${WORKDIR}/contrib/modules"
