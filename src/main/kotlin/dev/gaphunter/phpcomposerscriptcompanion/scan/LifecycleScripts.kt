package dev.gaphunter.phpcomposerscriptcompanion.scan

/**
 * Composer script event names Composer itself invokes automatically at
 * specific points in its own command lifecycle -- never real orphans
 * even with no text reference anywhere, because their "caller" is the
 * Composer CLI itself. Curated directly from Composer's own documented
 * event list (getcomposer.org/doc/articles/scripts.md, confirmed
 * 2026-08-23, not guessed): Command Events, Installer Events, Package
 * Events, and Plugin Events sections.
 */
object LifecycleScripts {

    private val ALWAYS_USED: Set<String> = setOf(
        // Command Events
        "pre-install-cmd", "post-install-cmd",
        "pre-update-cmd", "post-update-cmd",
        "pre-status-cmd", "post-status-cmd",
        "pre-archive-cmd", "post-archive-cmd",
        "pre-autoload-dump", "post-autoload-dump",
        "post-root-package-install", "post-create-project-cmd",
        // Installer Events
        "pre-operations-exec",
        // Package Events
        "pre-package-install", "post-package-install",
        "pre-package-update", "post-package-update",
        "pre-package-uninstall", "post-package-uninstall",
        // Plugin Events
        "init", "command", "pre-file-download", "post-file-download",
        "pre-command-run", "pre-pool-create",
    )

    /**
     * True when [name] is unconditionally treated as used: either a
     * fixed Composer event name, or a `pre-`/`post-` prefix of a real
     * sibling script name in [allScriptNames] -- teams commonly define
     * their own `pre-foo`/`post-foo` pair around a custom `foo` script,
     * mirroring Composer's own naming convention even for non-built-in
     * event names.
     */
    fun isAlwaysUsed(name: String, allScriptNames: Set<String>): Boolean {
        if (name in ALWAYS_USED) return true

        val base = when {
            name.startsWith("pre-") -> name.removePrefix("pre-")
            name.startsWith("post-") -> name.removePrefix("post-")
            else -> return false
        }
        return base.isNotEmpty() && base in allScriptNames
    }
}
