# Design the documentation site's own visual identity

Difficulty: Medium

## Goal

The generator scaffold currently reuses the structure and look of the rontolisp docs
(dark developer-console aesthetic). Give the YAVI documentation site its own intentional
web design. Load the `frontend-design` skill before starting and follow it.

## Scope

- Redesign `doc-site/src/main/resources/assets/docs.css` and, where the markup must
  change, `HtmlTemplate` (top bar, sidebar, content, TOC, previous/next, search dialog,
  version/language switchers, runnable cells and output). Do not change the content
  contract, URLs, nav format or runtime API.
- Choose a direction that fits YAVI as a Java validation library and that is visibly
  different from rontolisp: a clear visual identity (typography, color, spacing, code
  presentation), not a re-skin of the same dark theme. Support light and dark according
  to `prefers-color-scheme`, or commit to one mode if the direction is stronger that way.
- The site is documentation first: readable long-form prose, dense tables (built-in
  constraints message keys), many Java code blocks, and a persistent navigation tree.
  Code blocks, inline code, tables, notes/warnings, anchors and the "On this page" TOC
  must be legible and consistent.
- Keep the interaction surfaces usable: mobile navigation, search dialog (Ctrl/Cmd+K),
  version and language switchers, copy buttons, Run cells with loading/error states.
- Accessibility: semantic landmarks, visible focus, sufficient contrast, keyboard
  operation. Do not rely on color alone.
- No external web fonts if a good system stack avoids a network dependency; if a font is
  essential, self-host it in the assets.
- Keep CSS/JS free of build tooling. No emoji in the UI unless it carries meaning.

## Verification

- Build the generator, generate the site and inspect it in a browser at desktop and
  mobile widths, light and dark, using the placeholder or migrated content.
- Check the search dialog, both switchers, the runnable cell (stub runtime), tables and a
  long page (built-in constraints) for layout problems.
- No regression in the generator tests.

## Brand palette

Use the YAVI logo's own palette and keep the site visually consistent with it:

- deep plum `#590f3b` (primary brand color)
- rose `#b15c7a` (accent)
- gold `#f7e093` (restrained highlight; not a broad background)

The logo source is the image linked from the README. Place it in the brand area of the
header or sidebar from `doc-site/src/main/resources/assets/`, and match the favicon to the
palette. Check contrast (WCAG AA) in both color schemes.

## One logo only

The logo appears once on the page: either in the header brand area or above the sidebar
navigation, never both. Pick the placement that makes the stronger layout and remove the
other instance.
