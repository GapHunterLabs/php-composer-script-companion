# PHP Composer Script Companion

Gutter icon per `"scripts"` entry of an open `composer.json`, showing
whether it's used or an orphan candidate — checks the script's own
composer.json (calling another script), GitHub Actions/GitLab CI/
CircleCI configs, and README.md for a real `composer run-script <name>`
or `composer <name>` invocation.

## Why it exists

Same real problem `unused-npm-script-companion` already solves for
npm, ported to PHP/Composer: scripts pile up in `composer.json` over
time, and there's no way to tell at a glance which ones are still
actually called anywhere versus safe to delete.

## Why built this way

- **Same proven architecture as `unused-npm-script-companion`**, ported
  to Composer's real CLI shapes (`composer run-script <name>`,
  `composer <name>`) and script event names (`pre-install-cmd`,
  `post-autoload-dump`, etc. — Composer's equivalent of npm lifecycle
  scripts), both confirmed against Composer's own documentation.
- **Real JSON PSI, not a hand-rolled parser** — Composer scripts can be
  a single string or an array of commands; both are handled.

## v0.1 scope — stated honestly, not exhaustively

CI scope: GitHub Actions, GitLab CI, and CircleCI (not Jenkinsfile/
Azure Pipelines) — a real, documented gap, not a hidden one.

## Usage

Open any `composer.json` with a `"scripts"` section. Each entry shows a
used/orphan-candidate icon in the gutter.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
