#!/bin/bash
set -ex

# Build script for Travis-CI.
SCRIPTDIR=$(cd $(dirname "$0") && pwd)
ROOTDIR="$SCRIPTDIR/../.."
HOMEDIR="$ROOTDIR/../"
WHISKDIR="$HOMEDIR/openwhisk"

export OPENWHISK_HOME=${OPENWHISK_HOME:=$WHISKDIR}

# Build OpenWhisk
cd $OPENWHISK_HOME
TERM=dumb ./gradlew install
