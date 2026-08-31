<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# PHP Composer Script Companion Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.2.0...HEAD
[0.2.0]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.1.1...0.2.0
[0.1.1]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/php-composer-script-companion/commits/0.1.0
