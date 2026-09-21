# project-sources (demo)

Open `composer.json` here: every script has a gutter icon, and the tooltip
says where the plugin found it being used.

| Script | Where it's used |
|---|---|
| `lint`, `phpunit` | reused by `test` as `@lint`, `@phpunit` |
| `test` | `composer test` in `.github/workflows/ci.yml` |
| `phpstan`, `smoke` | picked from a CI matrix: `composer ${{ matrix.task }}` |
| `release` | `composer run release` in `Makefile` |
| `docs:build` | `composer run-script --timeout=0 docs:build` in `CONTRIBUTING.md` |
| `db:seed` | `php composer.phar db:seed` in `docs/setup/database.md` |
| `agent:check` | `composer agent:check` in `AGENTS.md` |
| `coverage` | through its alias: `composer cov` in `bin/ci.sh` |
| `hook:pre-commit` | `composer hook:pre-commit` in `captainhook.json` |
| `legacy-import` | **nowhere** -- a real orphan candidate |
| `fix` | **nowhere**: CI runs `vendor/bin/php-cs-fixer fix` directly, not the script |
