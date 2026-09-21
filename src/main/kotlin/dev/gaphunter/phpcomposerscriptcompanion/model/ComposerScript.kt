package dev.gaphunter.phpcomposerscriptcompanion.model

/**
 * One entry of a `composer.json`'s `"scripts"` object -- name, command text
 * (array values joined with newline), the name literal's text offset for
 * re-anchoring a gutter icon, and its `"scripts-aliases"` (Composer 2.7+),
 * which run it just as well as its own name does.
 */
data class ComposerScript(
    val name: String,
    val command: String,
    val nameLiteralStartOffset: Int,
    val aliases: List<String> = emptyList(),
) {
    /** Every name this script can be invoked by. */
    val invocationNames: List<String> get() = listOf(name) + aliases
}

/** Outcome of checking whether one [ComposerScript] is used somewhere. */
enum class UsageVerdict {
    /** Called by another script in the same composer.json. */
    USED_IN_OWN_COMPOSER_JSON,

    /** Invoked (`composer run-script <name>`, `composer <name>`...) in a CI config or another project file: Makefile, Dockerfile, compose file, git hooks. */
    USED_IN_CI,

    /** Mentioned in README.md. */
    USED_IN_README,

    /** A Composer script event name (or the pre/post of a real sibling script) -- never flagged. */
    LIFECYCLE_HOOK,

    /** No reference found anywhere this plugin looks -- a real orphan candidate. */
    ORPHANED,
}

data class ScriptUsage(
    val script: ComposerScript,
    val verdict: UsageVerdict,
    val detail: String,
)
