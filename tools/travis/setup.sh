#!/bin/bash

SCRIPTDIR=$(cd $(dirname "$0") && pwd)
HOMEDIR="$SCRIPTDIR/../../../"
DEPLOYDIR="$HOMEDIR/openwhisk/catalog/extra-packages/packageDeploy"

# jshint support
sudo apt-get -y install nodejs npm
sudo npm install -g jshint

# clone utilties repo. in order to run scanCode.py
cd $HOMEDIR
git clone https://github.com/apache/incubator-openwhisk-utilities.git

# shallow clone OpenWhisk repo.
git clone --depth 1 https://github.com/apache/incubator-openwhisk.git openwhisk

# shallow clone deploy package repo.
git clone --depth 1 https://github.com/apache/incubator-openwhisk-package-deploy $DEPLOYDIR

cd openwhisk

# use runtimes.json that defines python-jessie & IBM Node.js 8
rm -f ansible/files/runtimes.json
cp $HOMEDIR/template-hello-world/ansible/files/runtimes.json ansible/files/runtimes.json

./tools/travis/setup.sh
