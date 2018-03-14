#!/bin/bash
# Build script for Travis-CI.

SCRIPTDIR=$(cd $(dirname "$0") && pwd)
ROOTDIR="$SCRIPTDIR/../../.."
WHISKDIR="$ROOTDIR/openwhisk"
PACKAGESDIR="$WHISKDIR/catalog/extra-packages"
IMAGE_PREFIX="testing"

# Set Environment
export OPENWHISK_HOME=$WHISKDIR

cd $WHISKDIR

tools/build/scanCode.py "$SCRIPTDIR/../.."

# Build Openwhisk
./gradlew distDocker -PdockerImagePrefix=${IMAGE_PREFIX}

docker pull ibmfunctions/action-nodejs-v8
docker tag ibmfunctions/action-nodejs-v8 ${IMAGE_PREFIX}/action-nodejs-v8

docker pull ibmfunctions/action-python-v3
docker tag ibmfunctions/action-python-v3 ${IMAGE_PREFIX}/action-python-v3

docker pull ibmfunctions/action-swift-v4.1
docker tag ibmfunctions/action-swift-v4.1 ${IMAGE_PREFIX}/action-swift-v4.1

cd $WHISKDIR/ansible

# Deploy Openwhisk
ANSIBLE_CMD="ansible-playbook -i environments/local -e docker_image_prefix=${IMAGE_PREFIX}"

$ANSIBLE_CMD setup.yml
$ANSIBLE_CMD prereq.yml
$ANSIBLE_CMD couchdb.yml
$ANSIBLE_CMD initdb.yml
$ANSIBLE_CMD wipe.yml
$ANSIBLE_CMD openwhisk.yml
$ANSIBLE_CMD postdeploy.yml

cd $WHISKDIR

VCAP_SERVICES_FILE="$(readlink -f $WHISKDIR/../tests/credentials.json)"

#update whisk.properties to add tests/credentials.json file to vcap.services.file, which is needed in tests
WHISKPROPS_FILE="$WHISKDIR/whisk.properties"
sed -i 's:^[ \t]*vcap.services.file[ \t]*=\([ \t]*.*\)$:vcap.services.file='$VCAP_SERVICES_FILE':'  $WHISKPROPS_FILE
cat whisk.properties

WSK_CLI=$WHISKDIR/bin/wsk
AUTH_KEY=$(cat $WHISKDIR/ansible/files/auth.whisk.system)
EDGE_HOST=$(grep '^edge.host=' $WHISKPROPS_FILE | cut -d'=' -f2)

# Place this template in correct location to be included in packageDeploy
mkdir -p $PACKAGESDIR/preInstalled/ibm-functions
cp -r $ROOTDIR/template-messagehub-trigger $PACKAGESDIR/preInstalled/ibm-functions/

# Install the deploy package
cd $PACKAGESDIR/packageDeploy/packages
source $PACKAGESDIR/packageDeploy/packages/installCatalog.sh $AUTH_KEY $EDGE_HOST $WSK_CLI

# Install fake messagehub package
$WSK_CLI package create /whisk.system/messaging --apihost $EDGE_HOST --auth $AUTH_KEY --shared yes -i
$WSK_CLI action create /whisk.system/messaging/messageHubFeed --copy /whisk.system/utils/echo --apihost $EDGE_HOST --auth $AUTH_KEY -i

# Test
cd $ROOTDIR/template-messagehub-trigger
./gradlew :tests:test

# Toggle to run a single test
#./gradlew tests:test -Dtest.single=CloudantTests
