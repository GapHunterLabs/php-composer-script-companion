package dev.gaphunter.phpcomposerscriptcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.phpcomposerscriptcompanion.model.ScriptUsage
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict
import dev.gaphunter.phpcomposerscriptcompanion.parse.ComposerJsonParser
import dev.gaphunter.phpcomposerscriptcompanion.review.ReviewPrompt
import dev.gaphunter.phpcomposerscriptcompanion.scan.ComposerScriptAudit

/**
 * Gutter icon per `"scripts"` entry of an open `composer.json`, showing
 * whether it's used or orphaned -- same design as
 * `unused-npm-script-companion`'s `UnusedNpmScriptLineMarkerProvider`,
 * ported to Composer's script event names/CLI shapes.
 */
class UnusedComposerScriptLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Unused Composer scripts"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val jsonFile = elements.firstOrNull()?.containingFile as? JsonFile ?: return
        if (jsonFile.virtualFile?.name != "composer.json") return

        val scripts = ComposerJsonParser.parseScripts(jsonFile)
        if (scripts.isEmpty()) return

        val usages = ComposerScriptAudit.audit(scripts, jsonFile.virtualFile?.parent)
        val usageByOffset = usages.associateBy { it.script.nameLiteralStartOffset }

        for (element in elements) {
            val nameLiteral = nameLiteralOf(element) ?: continue
            val usage = usageByOffset[nameLiteral.textRange.startOffset] ?: continue
            result.add(buildMarker(nameLiteral, usage))

            // Only the orphaned verdict is a real, actionable finding --
            // the "used" icon shows up on ordinary, healthy scripts too
            // and would inflate the CTA counter on a normal composer.json.
            if (usage.verdict == UsageVerdict.ORPHANED) {
                val path = jsonFile.virtualFile?.path ?: continue
                val lineNumber = jsonFile.viewProvider.document?.getLineNumber(nameLiteral.textRange.startOffset) ?: -1
                ReviewPrompt.recordHit(jsonFile.project, "$path:$lineNumber")
            }
        }
    }

    private fun nameLiteralOf(element: PsiElement): JsonStringLiteral? {
        val literal = element as? JsonStringLiteral ?: return null
        val property = literal.parent as? JsonProperty ?: return null
        if (property.nameElement !== literal) return null
        return literal
    }

    /** Leaf-anchored -- same `firstChild` leaf-descent as `unused-npm-script-companion`. */
    private fun buildMarker(nameLiteral: JsonStringLiteral, usage: ScriptUsage): LineMarkerInfo<PsiElement> {
        val leaf = nameLiteral.firstChild ?: nameLiteral
        val tooltip = tooltipFor(usage)
        return LineMarkerInfo(
            leaf,
            nameLiteral.textRange,
            iconFor(usage.verdict),
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }

    private fun iconFor(verdict: UsageVerdict) = when (verdict) {
        UsageVerdict.ORPHANED -> ComposerScriptIcons.ORPHANED
        else -> ComposerScriptIcons.USED
    }

    private fun tooltipFor(usage: ScriptUsage): String {
        val prefix = when (usage.verdict) {
            UsageVerdict.ORPHANED -> "Possibly unused Composer script"
            else -> "Used Composer script"
        }
        return "$prefix \"${usage.script.name}\": ${usage.detail}"
    }
}
