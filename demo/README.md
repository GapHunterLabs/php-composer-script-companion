# Demo data for screenshots

`composer.json` — `test`/`ci` are used (called from each other),
`post-install-cmd` is a real Composer event (always used),
`generate-invoices-2023` is a realistic orphan (an old one-off script
nobody calls anymore).

## How to get the screenshot

1. `./gradlew runIde` from `php-composer-script-companion`, open this
   `demo/` folder as the project.
2. Full Screen, open `composer.json` — gutter icons should show
   `test`/`ci`/`post-install-cmd` as used and `generate-invoices-2023`
   as an orphan candidate.
3. Screenshot with all 4 icons visible, save into
   `php-composer-script-companion/docs/screenshots/`. Close the
   sandbox.
