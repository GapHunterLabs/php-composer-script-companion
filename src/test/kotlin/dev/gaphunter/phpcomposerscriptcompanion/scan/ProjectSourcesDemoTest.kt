package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.json.psi.JsonFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict.ORPHANED
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict.USED_IN_CI
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict.USED_IN_OWN_COMPOSER_JSON
import java.io.File

/**
 * The demo project read from disk (demo/project-sources, the same one its
 * README documents for a manual look in the IDE): every script's verdict
 * must be exactly this -- each used a different way, two really unused.
 */
class ProjectSourcesDemoTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = File("demo").absolutePath

    fun testEveryScriptGetsTheVerdictItsReadmeDocuments() {
        myFixture.copyDirectoryToProject("project-sources", "")
        val json = myFixture.configureFromTempProjectFile("composer.json") as JsonFile
        val verdicts = ComposerScriptAudit.audit(json).associate { it.script.name to it.verdict }
        assertEquals(
            mapOf<String, UsageVerdict>(
                "test" to USED_IN_CI,
                "lint" to USED_IN_OWN_COMPOSER_JSON,
                "phpunit" to USED_IN_OWN_COMPOSER_JSON,
                "phpstan" to USED_IN_CI,
                "smoke" to USED_IN_CI,
                "release" to USED_IN_CI,
                "docs:build" to USED_IN_CI,
                "db:seed" to USED_IN_CI,
                "agent:check" to USED_IN_CI,
                "coverage" to USED_IN_CI,
                "hook:pre-commit" to USED_IN_CI,
                "legacy-import" to ORPHANED,
                "fix" to ORPHANED,
            ),
            verdicts,
        )
    }

    fun testWhereEachUseWasFound() {
        myFixture.copyDirectoryToProject("project-sources", "")
        val json = myFixture.configureFromTempProjectFile("composer.json") as JsonFile
        val details = ComposerScriptAudit.audit(json).associate { it.script.name to it.detail }
        assertEquals("called from Makefile", details["release"])
        assertEquals("called from CONTRIBUTING.md", details["docs:build"])
        assertEquals("called from docs/setup/database.md", details["db:seed"])
        assertEquals("called from AGENTS.md", details["agent:check"])
        assertEquals("called from bin/ci.sh", details["coverage"])
        assertEquals("called from captainhook.json", details["hook:pre-commit"])
        assertEquals("possibly called from .github/workflows/ci.yml, through a variable", details["smoke"])
    }
}
