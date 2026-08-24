package dev.gaphunter.phpcomposerscriptcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/** End-to-end via the real daemon slow-line-marker pass -- same confirmed pattern as `unused-npm-script-companion`'s `UnusedNpmScriptLineMarkerProviderTest` (`DaemonCodeAnalyzerImpl.getLineMarkers`, not a guessed API). */
class UnusedComposerScriptLineMarkerProviderTest : BasePlatformTestCase() {

    private fun collectMarkers(): List<LineMarkerInfo<*>> {
        myFixture.doHighlighting()
        return DaemonCodeAnalyzerImpl.getLineMarkers(myFixture.editor.document, project)
    }

    private fun tooltipFor(scriptName: String): String {
        val marker = collectMarkers().first { it.element?.text == "\"$scriptName\"" }
        return marker.lineMarkerTooltip ?: ""
    }

    fun `test an orphaned script gets the warning tooltip`() {
        myFixture.configureByText(
            "composer.json",
            """{ "scripts": { "old-task": "php old.php" } }""",
        )
        assertTrue(tooltipFor("old-task").contains("Possibly unused Composer script"))
    }

    fun `test a used script gets the used tooltip, not the warning`() {
        myFixture.configureByText(
            "composer.json",
            """{ "scripts": { "build": "composer lint", "lint": "phpcs" } }""",
        )
        assertTrue(tooltipFor("lint").contains("Used Composer script \"lint\""))
    }

    fun `test a non-composer-json file is never scanned`() {
        myFixture.configureByText(
            "package.json",
            """{ "scripts": { "old-task": "node old.js" } }""",
        )
        assertTrue(collectMarkers().none { it.lineMarkerTooltip?.contains("Composer script") == true })
    }
}
