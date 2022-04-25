#!/usr/bin/env bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../"
cd ${ROOT_DIR}

print_usage() {
  cat <<EOF
Usage: $(basename $0) new_splunk_javaagent_version

All versions MUST NOT begin with 'v'. Example: 1.2.3".
EOF
}

if [[ $# < 1 ]]
then
  print_usage
  exit 1
fi

new_splunk_javaagent_version=$1

# MacOS requires passing backup file extension to in-place sed
if [[ $(uname -s) == "Darwin" ]]
then
  sed_flag='-i.tmp'
else
  sed_flag='-i'
fi

agent_version_args=(
  -e "/<!--DEV_DOCS_WARNING-->/r dev_docs_warning.md.tmp"

  # update SNAPSHOT link
  -e "s https://oss.sonatype.org/content/repositories/snapshots/com/splunk/splunk-otel-javaagent/${splunk_current_version}-SNAPSHOT/ https://oss.sonatype.org/content/repositories/snapshots/com/splunk/splunk-otel-javaagent/${splunk_next_version}-SNAPSHOT/ g"
)

sed ${sed_flag} \
  -e "s/static final String LATEST_VERSION = \".*\"/static final String LATEST_VERSION = \"$new_splunk_javaagent_version\"/" \
  src/test/java/io/opentelemetry/agents/AgentVersion.java

