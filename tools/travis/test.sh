#!/bin/bash

set -ex

# Build script for Travis-CI.

SCRIPTDIR=$(cd $(dirname "$0") && pwd)
ROOTDIR="$SCRIPTDIR/../.."
WHISKDIR="$ROOTDIR/../openwhisk"

export OPENWHISK_HOME=$WHISKDIR

cd ${ROOTDIR}
# Run the cloudant tests for travis
./gradlew tests:test -Dtest.single=MessageHubTests
