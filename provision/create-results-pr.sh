#!/bin/bash

# Creates a PR into gh-pages branch from the results

MYDIR=$(dirname $0)
RESULTS=${MYDIR}/../results
REV=$(ls "${RESULTS}")
NEW_BRANCH="results_${REV}"

set -e

echo ">>> Setting GnuPG configuration ..."
mkdir -p ~/.gnupg
chmod 700 ~/.gnupg
cat > ~/.gnupg/gpg.conf <<EOF
no-tty
pinentry-mode loopback
EOF

echo ">>> Importing secret key ..."
cat > /tmp/sk <<EOF
${GITHUB_BOT_GPG_KEY}
EOF
gpg --batch --allow-secret-key-import --import /tmp/sk
rm /tmp/sk

echo ">>> Setting up git config options"
GPG_KEY_ID=$(gpg2 -K --keyid-format SHORT | grep '^ ' | tr -d ' ')
git config --global user.name overhead-results
git config --global user.email ssg-srv-gh-o11y-gdi@splunk.com
git config --global gpg.program gpg
git config --global user.signingKey ${GPG_KEY_ID}

git clone https://srv-gh-o11y-gdi:"${GITHUB_TOKEN}"@github.com/signalfx/splunk-otel-java-overhead-test.git github-clone
cd github-clone
git checkout gh-pages
git checkout -b ${NEW_BRANCH}
cd ..

echo "Setting up a new pull request for results data: ${REV} results"
MSG="Add test results: ${REV}"

rsync -avv --progress "${RESULTS}/${REV}" github-clone/results/
cd github-clone
echo "Results list: " && ls -l results/
ls -1 results/ | grep -v README | grep -v index.txt | grep -v latest.yaml > results/index.txt
echo "Copying latest yaml..."
cp results/${REV}/results.yaml results/latest.yaml
echo "Adding new files to changelist"
git add results/index.txt
git add results/latest.yaml
git add results/${REV}/*
echo "Committing changes..."
git commit -S -am "[automated] $MSG"
echo "Pushing results to remote branch ${NEW_BRANCH}"
git push https://srv-gh-o11y-gdi:"${GITHUB_TOKEN}"@github.com/signalfx/splunk-otel-java-overhead-test.git ${NEW_BRANCH}

echo "Running PR create command:"
gh pr create \
  --title "$MSG" \
  --body "$MSG" \
  --label automated \
  --base gh-pages \
  --head "$NEW_BRANCH"
