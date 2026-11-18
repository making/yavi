# Agent Skill

An AI coding agent that knows Bean Validation still writes wrong YAVI code: it validates an object after creating it, reaches for a constraint the type does not have, and handles a result with `getErrors()` that no longer exists. The **agent skill** closes that gap. It is this documentation packaged as a Claude skill: a `SKILL.md` carrying the working rules, plus every page here as a bundled reference, generated on each deploy from the same Markdown, so it cannot drift from what you are reading.

## Install into Claude Code

The skill is published as a plugin. Add the marketplace and install it:

```bash
claude plugin marketplace add https://yavi.ik.am/skill/marketplace.json
claude plugin install yavi@yavi
```

The same two steps work inside a session as `/plugin marketplace add ...` and `/plugin install yavi@yavi`. Add `--scope project` to the first command to declare the marketplace in the repository instead of your user settings, so everyone working on the checkout gets the same offer.

Nothing else is needed: the agent consults the skill by itself when a task involves YAVI -- a `Yavi.schema()` chain, a `ValidatorBuilder`, or a request that names the library.

```bash
claude plugin update yavi@yavi     # take a newer version
claude plugin uninstall yavi@yavi  # remove it
claude plugin list                 # what is installed
```

Claude Code re-reads the marketplace file from that URL, so a new version becomes available without you changing anything -- see [Staying current](#staying-current) for how the version moves.

## Install without the plugin system

A skill is a directory, and Claude Code reads every skill under `~/.claude/skills` (yours) or `.claude/skills` (the repository's). If you would rather drop it in than register a marketplace:

```bash
mkdir -p ~/.claude/skills && \
  curl -sSL https://yavi.ik.am/skill/yavi-skill.tar.gz | tar xz -C ~/.claude/skills
```

Swap the target for `.claude/skills` to install it into the project instead. Either way you end up with `skills/yavi/SKILL.md` and `skills/yavi/references/`; `/skills` lists what is loaded, and removing the `yavi` directory uninstalls it. Nothing updates it for you, so this is the path where you check the version yourself.

## Other agents and hosts

| File | Use |
| --- | --- |
| [marketplace.json](https://yavi.ik.am/skill/marketplace.json) | the plugin marketplace, added by URL as above |
| [yavi-plugin.zip](https://yavi.ik.am/skill/yavi-plugin.zip) | the plugin itself, if you install plugins some other way |
| [yavi-skill.tar.gz](https://yavi.ik.am/skill/yavi-skill.tar.gz) | the bare skill directory, for a `skills` folder |
| [yavi.skill](https://yavi.ik.am/skill/yavi.skill) | the same tree as a zip, to upload where a skill is uploaded rather than unpacked |
| [SKILL.md](https://yavi.ik.am/skill/yavi/SKILL.md) | the skill body alone, readable in place |
| [yavi-full.md](https://yavi.ik.am/skill/yavi-full.md) | documentation and skill as ONE Markdown file, for a tool that has no skill loader |

## Staying current

The skill is versioned `<documented version>.<number of commits that can change it>`, so it moves only when a page or the generator does. A rebuild that produces the same bytes keeps the published version, so an unchanged documentation does not ask every installation to re-download itself. As a plugin, `claude plugin update yavi@yavi` takes the new one. Installed by hand, compare your copy against the published version and reinstall when they differ:

```bash
head -3 ~/.claude/skills/yavi/SKILL.md            # version: 0.18.42
curl -sSL https://yavi.ik.am/skill/VERSION
```

Reinstalling is the same command as installing -- it overwrites in place. [version.json](https://yavi.ik.am/skill/version.json) carries the same version plus the commit it was built from and a hash of the content, if you would rather check it from a script.

## What is inside

`SKILL.md` states the rules an agent with Bean Validation or Zod habits gets wrong: which style to choose, that a schema is not a validator until `fromMap` or `apply` ends it, that a required property is not written as `notNull()`, how `Validated` is handled with an exhaustive `switch`, and where the Kotlin helpers differ. It inlines [Getting Started](getting-started.md) as the quickstart, and carries a complete Spring MVC controller -- schema, `@ModelAttribute`, `BindingResult` and the Thymeleaf form -- for the shape a request boundary has. Under `references/`:

- `contents.md`, every page of this documentation by title.
- every page of this documentation, verbatim, at the same relative paths -- so a detail the skill needs is a file read away, with no network.
- the pages of the site's runnable examples as they are; the expected output is kept in the hidden `run:` comment, and the `Run` button is simply absent.

The skill mirrors one documentation version, the newest one, in English.
