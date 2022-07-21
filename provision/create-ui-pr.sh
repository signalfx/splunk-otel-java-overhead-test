#!/bin/bash

# Creates a PR into gh-pages branch with changes from the web ui

MYDIR=$(dirname $0)
NOW=$(date +'%Y%m%d%H%M%S')
NEW_BRANCH="gh-pages-${NOW}"

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

echo "Copying web ui files to gh-pages branch"
cp ../web/* .
for f in $(find . -type f -depth 1 -not -path '*/.*') ; do
   git add "$f"

echo "Setting up a new pull request for gh-pages changes"
git status

git commit -S -am "[automated] Updating gh-pages web ui"
echo "Pushing results to remote branch ${NEW_BRANCH}"
git push https://srv-gh-o11y-gdi:"${GITHUB_TOKEN}"@github.com/signalfx/splunk-otel-java-overhead-test.git ${NEW_BRANCH}

echo "Running PR create command:"
gh pr create \
  --title "[automated] Update gh-pages web ui" \
  --body "Update gh-pages web ui" \
  --label automated \
  --base gh-pages \
  --head "$NEW_BRANCH"
