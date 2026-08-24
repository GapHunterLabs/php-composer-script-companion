package dev.gaphunter.phpcomposerscriptcompanion.scan

/**
 * Finds real invocations of a Composer script by name inside arbitrary
 * text -- same narrow-matching discipline as
 * `unused-npm-script-companion`'s `ScriptReferenceMatcher`. Only the
 * real runner invocation shapes count, confirmed against Composer's own
 * documentation (getcomposer.org/doc/articles/scripts.md):
 *
 * - `composer run-script <name>` (the canonical, explicit form)
 * - `composer <name>` (Composer's own documented shorthand -- any
 *   script not shadowing a built-in command can be run directly)
 */
object ScriptReferenceMatcher {

    private val NAME_CONTINUATION = "[A-Za-z0-9_:.@/-]"

    fun isReferenced(text: String, scriptName: String): Boolean {
        val escaped = Regex.escape(scriptName)
        val pattern = Regex(
            """\bcomposer\s+(?:run-script\s+)?$escaped(?!$NAME_CONTINUATION)""",
        )
        return pattern.containsMatchIn(text)
    }
}
