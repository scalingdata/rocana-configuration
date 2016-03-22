# Copyright (c) 2016 Rocana
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/bash
set -x

MVN=$(type -p mvn)
MVN_FLAGS="-B"

if [ -z "${ROCANA_CONFIGURATION_RELEASE_VERSION}" ] || [ -z "${ROCANA_CONFIGURATION_DEVELOPMENT_VERSION}" ]; then
  echo "You must set ROCANA_CONFIGURATION_RELEASE_VERSION and ROCANA_CONFIGURATION_DEVELOPMENT_VERSION before running this script."
  exit 1
fi

if [ -n "${WORKSPACE}" ]; then
  MVN_FLAGS="${MVN_FLAGS} -f ${WORKSPACE}/pom.xml"
  MVN_FLAGS="${MVN_FLAGS} -Dmaven.repo.local=${WORKSPACE}/.repository"
fi

# Set the version to the next release
${MVN} ${MVN_FLAGS} \
  versions:set \
    -DnewVersion=${ROCANA_CONFIGURATION_RELEASE_VERSION} \
    -DgenerateBackupPoms=false \
  || exit 1

# Commit and push the version number update
${MVN} ${MVN_FLAGS} \
  scm:add \
    -Dincludes=**/pom.xml \
    -Dexcludes=**/target/**/pom.xml \
  scm:checkin \
    -Dmessage="ROCANA-BUILD: Preparing for release ${ROCANA_CONFIGURATION_RELEASE_VERSION}" \
  || exit 1

# Deploy the release to the maven repo and tag the release in git
${MVN} ${MVN_FLAGS} \
  deploy \
  scm:tag \
    -Dtag=release-${ROCANA_CONFIGURATION_RELEASE_VERSION} \
  || exit 1

# Set the version to the next development version
${MVN} ${MVN_FLAGS} \
  versions:set \
    -DnewVersion=${ROCANA_CONFIGURATION_DEVELOPMENT_VERSION} \
    -DgenerateBackupPoms=false \
  || exit 1

# Commit and push the version number update
${MVN} ${MVN_FLAGS} \
  scm:add \
    -Dincludes=**/pom.xml \
    -Dexcludes=**/target/**/pom.xml \
  scm:checkin \
    -Dmessage="ROCANA-BUILD: Preparing for ${ROCANA_CONFIGURATION_DEVELOPMENT_VERSION} development" \
  || exit 1
