#!/bin/bash
CURRENT_VERSION=$(./get-release-version.sh)

./mvnw versions:set -DnewVersion=${CURRENT_VERSION} -DgenerateBackupPoms=false
git add pom.xml
git commit -m "Bump to ${CURRENT_VERSION}"