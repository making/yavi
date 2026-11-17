# Publish the versioned documentation site from this repository

Difficulty: Medium

## Scope

Depends on `.todo/084` and at least the English content items.

Add a GitHub Actions workflow that builds `doc-site` and publishes it to the existing
site `yavi.ik.am` (the gh-pages branch currently holds the AsciiDoc output and the
`CNAME`).

- Build the generator and render `doc-site/content` to the site root, so the version
  selector and `/0.17/en/`, `/0.17/ja/` paths are online.
- Keep `CNAME` (and any other repository-level file on gh-pages) intact.
- The old unversioned URLs (`/getting-started.html`, `/built-in-constraints.html`, ...)
  must keep working: emit redirect stubs from each old root page to the corresponding
  page of the default version/language, or configure the site so they resolve.
- `/` redirects to the default version and language.
- Trigger on changes to `doc-site/**` and the workflow file; do not run on pull requests
  that only change Java sources.
- Stop publishing from `../yavi-doc`: remove or disable its deploy workflow once this
  workflow has run successfully on `main`/`develop` and the site is verified. Note the
  switch in `doc-site/README.md`.

## Done when

A push builds and deploys the site, the version and language switchers work on the
published domain, old links resolve, and there is one owner of the published docs.
