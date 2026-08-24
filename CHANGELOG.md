<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# PHP Composer Script Companion Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/GapHunterLabs/php-composer-script-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/php-composer-script-companion/commits/0.1.0
