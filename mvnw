#!/usr/bin/env sh
set -eu

IMAGE="${MVNW_DOCKER_IMAGE:-maven:3.9.9-eclipse-temurin-21}"
WORKDIR="/workspace"

exec docker run --rm \
  -v "$(pwd):${WORKDIR}" \
  -v "${HOME}/.m2:/root/.m2" \
  -w "${WORKDIR}" \
  "${IMAGE}" mvn "$@"

