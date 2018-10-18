#!/bin/bash
set -ex

# Build script for Travis-CI.
SCRIPTDIR=$(cd $(dirname "$0") && pwd)
ROOTDIR="$SCRIPTDIR/../.."
HOMEDIR="$ROOTDIR/../"
WHISKDIR="$HOMEDIR/openwhisk"

export OPENWHISK_HOME=${OPENWHISK_HOME:=$WHISKDIR}

OPENWHISK_UTILS_HOME=${HOMEDIR}/incubator-openwhisk-utilities
OPENWHISK_SCANCODE_CFG=${ROOTDIR}/tools/build/scanCode.cfg

cd ${ROOTDIR}
"$OPENWHISK_UTILS_HOME/scancode/scanCode.py" --config "$OPENWHISK_SCANCODE_CFG" "${ROOTDIR}"

TERM=dumb ./gradlew :tests:checkScalafmtAll