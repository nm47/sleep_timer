#!/usr/bin/env bash
# scripts/env.sh
# Source me:  $ source scripts/env.sh

# --- Canonical SDK install ---------------------------------------------------
export ANDROID_HOME=/home/niels/projects/android
export ANDROID_NDK_VERSION=28.2.13676358
export ANDROID_NDK_HOME=${ANDROID_HOME}/ndk/${ANDROID_NDK_VERSION}
export PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}
