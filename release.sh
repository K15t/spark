#!/bin/bash

fail() {
    echo "$1";
    exit 1;
}

printUsage() {
    echo "Create a maven release with the specified version number.
No changes will be pushed to the remote git repository until the end of the build process.

Usage:
    $0 <RELEASE_VERSION> <DEVELOPMENT_VERSION>
"

    exit 1
}

MVN_COMMAND=""
if [[ -x "$(command -v mvn)" ]]; then
    MVN_COMMAND="mvn"
elif [[ -x "$(command -v atlas-mvn)" ]]; then
    MVN_COMMAND="atlas-mvn"
else
    fail "Could find neither 'mvn' nor 'atlas-mvn'. Make sure one of them is on your path."
fi


GIT_COMMAND=""
if [[ -x "$(command -v git)" ]]; then
    GIT_COMMAND="git"
else
    fail "Could not find 'git'. Make sure it is on your path."
fi

CURRENT_BRANCH=$(git rev-parse --symbolic-full-name --abbrev-ref HEAD)
if [[ "${CURRENT_BRANCH}" == "master" ]]; then
    fail "Cannot build release on master branch. Run this command on another branch instead."
fi

RELEASE_VERSION=""
if [[ $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    RELEASE_VERSION="$1"
else
    printUsage
fi

SNAPSHOT_VERSION=""
if [[ $2 =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    SNAPSHOT_VERSION="${2}-SNAPSHOT"
elif [[ $2 =~ ^[0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$ ]]; then
    SNAPSHOT_VERSION="${2}"
else
    printUsage
fi

echo "Release build plan:"
echo ""
echo "Release version: ${RELEASE_VERSION}"
echo "Next development version: ${SNAPSHOT_VERSION}"
echo ""
read -p "Create a release on branch \"${CURRENT_BRANCH}\", then merge to branch \"master\" and back to \"develop\"? [yn]: " -n 1 -r
echo    # (optional) move to a new line
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
	fail "Cancelled."
fi

set -o errexit
set -o errtrace
set -o nounset

PS4="RELEASE> "
set -x

${GIT_COMMAND} pull
if [[ "${CURRENT_BRANCH}" != "develop" ]]; then
    ${GIT_COMMAND} fetch origin master:master develop:develop
else
    ${GIT_COMMAND} fetch origin master:master
fi

${MVN_COMMAND} clean release:prepare release:perform --batch-mode -Pk15t-release -Darguments='-DskipTests -DskipITs' \
    -DpushChanges=false -DlocalCheckout=true -DautoVersionSubmodules=true -DpreparationGoals=clean \
    -Dtag="${RELEASE_VERSION}" -DreleaseVersion="${RELEASE_VERSION}" -DdevelopmentVersion="${SNAPSHOT_VERSION}";

${GIT_COMMAND} pull
if [[ "${CURRENT_BRANCH}" != "develop" ]]; then
    ${GIT_COMMAND} fetch origin master:master develop:develop
else
    ${GIT_COMMAND} fetch origin master:master
fi
${GIT_COMMAND} checkout master
${GIT_COMMAND} merge --no-ff "${CURRENT_BRANCH}" -m "Merge branch '${CURRENT_BRANCH}' with ${RELEASE_VERSION}"
${GIT_COMMAND} checkout develop
${GIT_COMMAND} merge --no-ff master -m "Merge branch 'master' into 'develop' with ${RELEASE_VERSION}"
${GIT_COMMAND} push origin master develop
${GIT_COMMAND} push --tags

set +x
if ! [[ "${CURRENT_BRANCH}" =~ ^(develop|master)$ ]]; then
    set -x
    ${GIT_COMMAND} branch -d "${CURRENT_BRANCH}"
    ${GIT_COMMAND} push origin --delete "${CURRENT_BRANCH}"
    set +x
fi

echo -e "\nAll done :-)"
