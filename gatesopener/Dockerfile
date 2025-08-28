# https://developers.home-assistant.io/docs/add-ons/configuration#add-on-dockerfile
ARG BUILD_FROM=ghcr.io/home-assistant/aarch64-base-ubuntu:20.04
FROM $BUILD_FROM

# Install Java runtime for JAR execution
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-17-jre-headless \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy root filesystem
COPY rootfs /

# Copy JAR file
COPY /bin/gates-opener-server-ktor.jar /usr/bin/gates_opener.jar