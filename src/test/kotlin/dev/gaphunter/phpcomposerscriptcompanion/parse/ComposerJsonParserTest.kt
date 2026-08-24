package dev.gaphunter.phpcomposerscriptcompanion.parse

import com.intellij.json.psi.JsonFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ComposerJsonParserTest : BasePlatformTestCase() {

    fun `test parses a simple string script`() {
        val file = myFixture.configureByText(
            "composer.json",
            """{ "scripts": { "test": "phpunit" } }""",
        ) as JsonFile
        val scripts = ComposerJsonParser.parseScripts(file)
        assertEquals(1, scripts.size)
        assertEquals("test", scripts[0].name)
        assertEquals("phpunit", scripts[0].command)
    }

    fun `test parses an array script as newline-joined commands`() {
        val file = myFixture.configureByText(
            "composer.json",
            """{ "scripts": { "post-install-cmd": ["echo one", "echo two"] } }""",
        ) as JsonFile
        val scripts = ComposerJsonParser.parseScripts(file)
        assertEquals(1, scripts.size)
        assertTrue(scripts[0].command.contains("echo one"))
        assertTrue(scripts[0].command.contains("echo two"))
    }

    fun `test a file with no scripts section returns an empty list, not a crash`() {
        val file = myFixture.configureByText("composer.json", """{ "require": {} }""") as JsonFile
        assertTrue(ComposerJsonParser.parseScripts(file).isEmpty())
        assertFalse(ComposerJsonParser.hasScriptsSection(file))
    }

    fun `test hasScriptsSection is true when scripts is an object, even empty`() {
        val file = myFixture.configureByText("composer.json", """{ "scripts": {} }""") as JsonFile
        assertTrue(ComposerJsonParser.hasScriptsSection(file))
    }
}
