# Converts an OCI image archive to docker-archive format, which can be
# loaded directly with `docker load -i <file>.docker.tar`.
#
# This class depends on the OCI image already being built (via image-oci),
# and uses skopeo-native to perform the conversion.
#
# Usage: Add "docker" to IMAGE_FSTYPES and inherit this class:
#   IMAGE_FSTYPES = "container oci docker"
#   inherit image-oci-docker

IMAGE_TYPEDEP:docker = "oci"
do_image_docker[depends] += "skopeo-native:do_populate_sysroot"

# The docker image name:tag to embed in the archive.
DOCKER_IMAGE_NAME ?= "${IMAGE_BASENAME}"
DOCKER_IMAGE_TAG ?= "${OCI_IMAGE_TAG}"

IMAGE_CMD:docker() {
    cd ${IMGDEPLOYDIR}

    oci_tar="${IMAGE_NAME}${IMAGE_NAME_SUFFIX}-oci-${OCI_IMAGE_TAG}-${OCI_IMAGE_ARCH}-linux.oci-image.tar"
    docker_tar="${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.docker.tar"

    if [ ! -f "${oci_tar}" ]; then
        bbfatal "OCI archive not found: ${oci_tar}"
    fi

    bbnote "Converting OCI archive to docker-archive format..."
    skopeo copy \
        "oci-archive:${oci_tar}" \
        "docker-archive:${docker_tar}:${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"

    # Create convenience symlink
    ln -sf "${docker_tar}" "${IMAGE_BASENAME}-${OCI_IMAGE_TAG}-docker.tar"
}
