#!/usr/bin/env bash

cd ..
./gradlew :common:yarn_install || exit 1
cd react || exit 1
yarn start
# Waiting for https://github.com/node-gradle/gradle-node-plugin/discussions/260 resolution
# ./gradlew :common:yarnStart