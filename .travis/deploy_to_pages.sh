#!/bin/bash

git clone --depth=1 --branch=gh-pages "https://github.com/kjkrVdxB/Java-HW" gh-pages
cd gh-pages
git rm -rf "$PROJECT_DIR"
mv "../$PROJECT_DIR/build/docs/javadoc" "$PROJECT_DIR"
git add --all
git config user.name "Travis CI"
git config user.email "travis@travis-ci.com"
git commit --message "Auto deploy from Travis CI"
git remote add upstream "https://$GITHUB_TOKEN@github.com/kjkrVdxB/Java-HW" >/dev/null 2>&1
git push upstream gh-pages >/dev/null 2>&1
