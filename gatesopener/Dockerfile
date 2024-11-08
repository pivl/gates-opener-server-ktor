# https://developers.home-assistant.io/docs/add-ons/configuration#add-on-dockerfile
ARG BUILD_FROM=ghcr.io/home-assistant/aarch64-base:3.20
FROM $BUILD_FROM

ARG BUILD_ARCH=aarch64

# install some deps
RUN apk add --no-cache gcompat

# Copy root filesystem
COPY rootfs /
COPY /bin/gates-opener-server-ktor-${BUILD_ARCH} /usr/bin/gates_opener