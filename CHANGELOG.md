<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# PHP Composer Script Companion Changelog

## [Unreleased]

## [0.3.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.3.0]

### Fixed

- A script reused from another script as `@name` -- Composer's own
  documented way, and the most common one -- was shown as possibly unused
  unless something else also called it. On 200 public PHP projects, a
  third of all custom scripts (521 of 1,579) are reused that way.
- `composer run <name>` (the documented alias of `run-script`), options
  before the name (`composer run-script --timeout=0 <name>`), and
  `composer.phar`, `bin/composer` or `@composer` weren't recognized as
  invocations.
- The description said CircleCI wasn't checked; it has been since 0.2.0.

### Added

- More places where projects really call their scripts, chosen from those
  200 projects: CONTRIBUTING.md, AGENTS.md and the other instructions
  files for coding agents, Markdown files in docs/, Makefile,
  package.json, captainhook.json and grumphp.yml, shell scripts in the
  root, bin/, scripts/ and build/, READMEs one folder down, composite
  GitHub Actions, and the other common CI systems (Travis CI, Bitbucket
  Pipelines, Jenkins, Azure Pipelines) plus Dockerfile and compose files.
- `scripts-aliases` (Composer 2.7+): running a script by one of its
  aliases counts as using it.
- A CI matrix read by a dynamic call (`composer ${{ matrix.task }}`,
  `composer run $SCRIPT`) counts for every script named in that file.

## [0.2.0]

### Added

- CircleCI support (`.circleci/config.yml`) alongside the existing
  GitHub Actions and GitLab CI scanning -- a script referenced only
  there is now correctly recognized as used instead of a false
  orphan-candidate.

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct orphaned-script findings (never
  counted for the informational "used" icon, only real actionable
  ones), a one-time notification asks whether to rate the plugin on
  Marketplace, with a permanent "Don't ask again" option. Standard
  mechanism used catalog-wide since 2026-08-24, rolled out to this
  plugin now.

## [0.1.0]

### Added

- Gutter icon per `"scripts"` entry of an open `composer.json`, showing
  used vs. orphan-candidate.
- Recognizes Composer's own documented script event names as
  always-used.
- Checks the script's own composer.json, GitHub Actions/GitLab CI
  configs, and README.md for real references.
- 100% static PSI/text analysis, no Composer CLI invocation, no
  network calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.3.1...HEAD
[0.3.1]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.1.1...0.2.0
[0.1.1]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/php-composer-script-companion/commits/0.1.0
