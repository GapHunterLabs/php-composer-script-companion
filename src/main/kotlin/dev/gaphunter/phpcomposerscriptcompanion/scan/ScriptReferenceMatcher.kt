package dev.gaphunter.phpcomposerscriptcompanion.scan

/**
 * Finds real invocations of a Composer script by name inside arbitrary
 * text -- same narrow-matching discipline as
 * `unused-npm-script-companion`'s `ScriptReferenceMatcher`. Only the
 * runner invocation shapes Composer documents count
 * (getcomposer.org/doc/articles/scripts.md and doc/03-cli.md):
 *
 * - `composer run-script <name>`, and its alias `composer run <name>`;
 * - `composer <name>` (any script can be run as a command of its own);
 * - with options in between (`composer -n run-script --timeout=0 <name>`),
 *   and with Composer spelled as `composer.phar`, a path to it
 *   (`bin/composer`), or `@composer` inside another script.
 *
 * Inside composer.json itself, [isReferencedFromScript] also accepts
 * Composer's own way of reusing a script: `@<name>` (`"test": ["@lint",
 * "@phpunit"]`). Every name is matched whole: `test` never matches
 * `test-integration` or `test:unit`.
 */
object ScriptReferenceMatcher {

    private const val NAME_CONTINUATION = "[A-Za-z0-9_:.@/-]"

    /** `-n`, `--no-dev`, `--timeout=0`, and the two options that take a separate value. */
    private const val OPTION = """(?:(?:-d|--working-dir|--timeout)\s+\S+|--?[A-Za-z][\w-]*(?:=\S+)?)"""

    private const val COMPOSER = """(?<![\w.-])(?:[\w./-]*/)?composer(?:\.phar)?"""

    fun isReferenced(text: String, scriptName: String): Boolean {
        val name = Regex.escape(scriptName)
        val pattern = Regex(
            """$COMPOSER(?:\s+$OPTION)*(?:\s+(?:run-script|run)(?:\s+$OPTION)*)?\s+$name(?!$NAME_CONTINUATION)""",
        )
        return pattern.containsMatchIn(text)
    }

    private val DYNAMIC_INVOCATION = Regex(
        """$COMPOSER(?:\s+$OPTION)*(?:\s+(?:run-script|run)(?:\s+$OPTION)*)?\s+(?:\$\{\{|\$\{?[A-Za-z_]|\$\()""",
    )

    /**
     * `composer ${{ matrix.task }}`, `composer run $SCRIPT`, `composer $(ARGS)`:
     * the script name comes from a variable, usually set in the same file (a
     * CI matrix, an `export`). Found on real pipelines; see [mentionsName].
     */
    fun hasDynamicInvocation(text: String): Boolean = DYNAMIC_INVOCATION.containsMatchIn(text)

    /**
     * The name written as a VALUE a variable can take: a list item
     * (`[phpstan, smoke]`, `- phpstan`), a mapping value (`task: phpstan`),
     * an assignment (`SCRIPT=phpstan`) or a quoted string. Not just any
     * occurrence: in `vendor/bin/php-cs-fixer fix` the word `fix` is an
     * argument of another tool, not the `fix` script. Comment lines (`#`,
     * as in YAML, shell and Makefiles) are skipped.
     */
    fun mentionsName(text: String, scriptName: String): Boolean {
        val name = Regex.escape(scriptName)
        val code = text.lineSequence().filterNot { it.trimStart().startsWith("#") }.joinToString("\n")
        return Regex(
            """(?:^[ \t]*-[ \t]*|[\[,][ \t]*|:[ \t]+|=|["'])$name(?:["']|[ \t]*(?:[,\]#]|$))""",
            RegexOption.MULTILINE,
        ).containsMatchIn(code)
    }

    /** A reference from another script's command: any [isReferenced] shape, or Composer's `@<name>`. */
    fun isReferencedFromScript(command: String, scriptName: String): Boolean {
        val name = Regex.escape(scriptName)
        return Regex("""(?<![\w@])@$name(?!$NAME_CONTINUATION)""").containsMatchIn(command) ||
            isReferenced(command, scriptName)
    }
}
