# https://developers.home-assistant.io/docs/add-ons/configuration#add-on-dockerfile
ARG BUILD_FROM
FROM $BUILD_FROM

ARG BUILD_ARCH
# Copy root filesystem
COPY rootfs /
COPY bin/gates-opener-server-ktor-${BUILD_ARCH} /usr/bin/gates-opener-server-ktor
