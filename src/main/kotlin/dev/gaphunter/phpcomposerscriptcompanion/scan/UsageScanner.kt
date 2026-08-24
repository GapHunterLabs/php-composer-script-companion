package dev.gaphunter.phpcomposerscriptcompanion.scan

import dev.gaphunter.phpcomposerscriptcompanion.model.ComposerScript
import dev.gaphunter.phpcomposerscriptcompanion.model.ScriptUsage
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict

/** Same verdict-priority design as `unused-npm-script-companion`'s `UsageScanner`, adapted for Composer. */
object UsageScanner {

    fun scan(
        scripts: List<ComposerScript>,
        ciFiles: Map<String, String>,
        readmeText: String?,
    ): List<ScriptUsage> {
        val allNames = scripts.map { it.name }.toSet()
        return scripts.map { script -> evaluate(script, scripts, allNames, ciFiles, readmeText) }
    }

    private fun evaluate(
        script: ComposerScript,
        allScripts: List<ComposerScript>,
        allNames: Set<String>,
        ciFiles: Map<String, String>,
        readmeText: String?,
    ): ScriptUsage {
        if (LifecycleScripts.isAlwaysUsed(script.name, allNames)) {
            return ScriptUsage(script, UsageVerdict.LIFECYCLE_HOOK, "Composer script event convention -- always runs automatically")
        }

        val caller = allScripts.firstOrNull { other ->
            other.name != script.name && ScriptReferenceMatcher.isReferenced(other.command, script.name)
        }
        if (caller != null) {
            return ScriptUsage(
                script,
                UsageVerdict.USED_IN_OWN_COMPOSER_JSON,
                "called from this composer.json's own \"${caller.name}\" script",
            )
        }

        val ciMatch = ciFiles.entries.firstOrNull { (_, text) -> ScriptReferenceMatcher.isReferenced(text, script.name) }
        if (ciMatch != null) {
            return ScriptUsage(script, UsageVerdict.USED_IN_CI, "called from ${ciMatch.key}")
        }

        if (readmeText != null && ScriptReferenceMatcher.isReferenced(readmeText, script.name)) {
            return ScriptUsage(script, UsageVerdict.USED_IN_README, "mentioned in README.md")
        }

        return ScriptUsage(script, UsageVerdict.ORPHANED, "no reference found in this composer.json, GitHub Actions/GitLab CI configs, or README.md")
    }
}
