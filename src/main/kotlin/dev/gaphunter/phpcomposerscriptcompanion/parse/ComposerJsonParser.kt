package dev.gaphunter.phpcomposerscriptcompanion.parse

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import dev.gaphunter.phpcomposerscriptcompanion.model.ComposerScript

/**
 * Reads the `"scripts"` object of an already-parsed `composer.json`
 * ([JsonFile]) via the bundled JSON plugin's real PSI -- same
 * "don't reinvent a parser for a format the platform already parses
 * correctly" principle proven by `unused-npm-script-companion`'s
 * `PackageJsonParser`. Composer scripts differ from npm's in one real
 * way: a script's value can be a single string OR an array of strings
 * (multiple commands run in sequence) -- both are handled, joined with
 * a newline for the array case so downstream text-matching sees every
 * command.
 */
object ComposerJsonParser {

    fun parseScripts(file: JsonFile): List<ComposerScript> {
        val root = file.topLevelValue as? JsonObject ?: return emptyList()
        val scriptsProperty = root.findProperty("scripts") ?: return emptyList()
        val scriptsObject = scriptsProperty.value as? JsonObject ?: return emptyList()

        return scriptsObject.propertyList.mapNotNull { property -> toComposerScript(property) }
    }

    fun hasScriptsSection(file: JsonFile): Boolean {
        val root = file.topLevelValue as? JsonObject ?: return false
        val scriptsProperty = root.findProperty("scripts") ?: return false
        return scriptsProperty.value is JsonObject
    }

    private fun toComposerScript(property: JsonProperty): ComposerScript? {
        val nameLiteral = property.nameElement as? JsonStringLiteral ?: return null
        val command = when (val value = property.value) {
            is JsonStringLiteral -> value.value
            is JsonArray -> value.valueList.filterIsInstance<JsonStringLiteral>().joinToString("\n") { it.value }
            else -> return null
        }
        return ComposerScript(
            name = property.name,
            command = command,
            nameLiteralStartOffset = nameLiteral.textRange.startOffset,
        )
    }
}
