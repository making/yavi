```
# Merge develop to main
git checkout main
git merge develop
./set-release-version.sh

# Create a tag and push it
VERSION=$(./get-release-version.sh)
git tag ${VERSION}
git push origin main
git push origin ${VERSION}

# Merge main to develop and bump the version
./merge-main-to-develop.sh
./set-next-patch-version.sh
# or
./set-next-minor-version.sh
# Update REAMD
git add README.md
git commit -m "Update README"
git push origin develop
```