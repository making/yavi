#!/bin/bash
set -ex

git checkout main
git pull origin main
git checkout develop
git merge main