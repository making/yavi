```
# Merge develop to main
git checkout main
git merge develop

# Update README (version)
git add README.md
git commit -m "Update README"
./set-release-version.sh

# Push main and wait for deployment
git push origin main
# Wait for GitHub Actions deployment to complete

# Create a tag and push it
VERSION=$(./get-release-version.sh)
git tag ${VERSION}
git push origin ${VERSION}

# Point the documentation site at the released version
# Before the release, the new documentation version is not the default and its
# yavi_release is "<VERSION>-SNAPSHOT" (see doc-site/content/versions.yaml). Once the
# artifact is on Maven Central, in doc-site/content/versions.yaml:
#   - default_version to "<VERSION>"
#   - yavi_release of "<VERSION>" to "<VERSION>"
git add doc-site/content/versions.yaml
git commit -m "Release the ${VERSION} documentation"
git push origin main
# Wait for GitHub Actions deployment to complete

# Merge main to develop and bump the version
./merge-main-to-develop.sh
./set-next-patch-version.sh
# or
./set-next-minor-version.sh
git push origin develop
```
