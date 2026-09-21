# PHP Composer Script Companion

Gutter icon per `"scripts"` entry of an open `composer.json`, showing
whether it's used or an orphan candidate. A script counts as used when
another script reuses it (`@name`), or when a real invocation
(`composer run-script <name>`, `composer run <name>`, `composer <name>`)
appears in the project's CI configs, build files, git hooks or
contributor docs — see [What it reads](#what-it-reads).

## Why it exists

Same real problem `unused-npm-script-companion` already solves for
npm, ported to PHP/Composer: scripts pile up in `composer.json` over
time, and there's no way to tell at a glance which ones are still
actually called anywhere versus safe to delete.

## Why built this way

- **Same proven architecture as `unused-npm-script-companion`**, ported
  to every invocation shape Composer documents
  (getcomposer.org/doc/articles/scripts.md and doc/03-cli.md):
  `composer run-script <name>` and its alias `composer run <name>`,
  `composer <name>`, options in between (`composer -n run-script
  --timeout=0 <name>`), `composer.phar`/`bin/composer`/`@composer`, a
  script reused from another one as `@name`, and `scripts-aliases`
  (Composer 2.7+). Script event names (`pre-install-cmd`,
  `post-autoload-dump`, etc. — Composer's equivalent of npm lifecycle
  scripts) are always treated as used.
- **Real JSON PSI, not a hand-rolled parser** — Composer scripts can be
  a single string or an array of commands; both are handled.

## What it reads

Fixed paths and one folder level only, never a recursive walk of the
project, next to the `composer.json` being checked:

- **CI:** `.github/workflows/*.yml`, `.github/actions/*/action.yml`,
  `.gitlab-ci.yml`, `.circleci/config.yml`, `.travis.yml`,
  `bitbucket-pipelines.yml`, `Jenkinsfile`, `azure-pipelines.yml`.
- **Build and containers:** `Makefile`, `package.json`, `Dockerfile`,
  `docker-compose.yml`/`compose.yaml`, `*.sh` in the root, `bin/`,
  `scripts/` and `build/`.
- **Git hooks:** `captainhook.json`, `grumphp.yml(.dist)`.
- **Docs:** `README.md`, `CONTRIBUTING.md` (root or `.github/`),
  `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md`, the
  Markdown files in `docs/` or `doc/` (up to two folders down, at most
  200), and a `README.md` one folder down. File names are matched
  ignoring case (`readme.md`, `makefile`).

A CI matrix read by a dynamic call (`composer ${{ matrix.task }}`,
`composer run $SCRIPT`) counts for every script named in that same file.

That list comes from a corpus of 200 public PHP projects: it's where the
scripts the plugin couldn't see before were actually being called. On
that corpus, the orphan candidates went from 1,312 (0.2.0) to 626, and
random samples of the remaining ones were searched for across each whole
repository to find where the plugin still wasn't looking.

**Not read, on purpose:** other folders, PHP code that runs Composer
itself, and commands only ever typed by hand. A script used only that way
shows as an orphan candidate — which is what the icon means: nothing in
the project calls it.

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
