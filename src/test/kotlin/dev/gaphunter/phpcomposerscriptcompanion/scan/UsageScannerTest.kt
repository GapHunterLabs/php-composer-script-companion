package dev.gaphunter.phpcomposerscriptcompanion.scan

import dev.gaphunter.phpcomposerscriptcompanion.model.ComposerScript
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class UsageScannerTest {

    private fun script(name: String, command: String) = ComposerScript(name, command, 0)

    @Test
    fun `a script called from another script in the same file is USED_IN_OWN_COMPOSER_JSON`() {
        val scripts = listOf(script("build", "composer lint"), script("lint", "phpcs"))
        val usages = UsageScanner.scan(scripts, emptyMap(), null)
        val lint = usages.first { it.script.name == "lint" }
        assertEquals(UsageVerdict.USED_IN_OWN_COMPOSER_JSON, lint.verdict)
    }

    @Test
    fun `a script referenced only in CI is USED_IN_CI`() {
        val scripts = listOf(script("test", "phpunit"))
        val ciFiles = mapOf(".github/workflows/ci.yml" to "run: composer test")
        val usages = UsageScanner.scan(scripts, ciFiles, null)
        assertEquals(UsageVerdict.USED_IN_CI, usages[0].verdict)
    }

    @Test
    fun `a script referenced only in README is USED_IN_README`() {
        val scripts = listOf(script("test", "phpunit"))
        val usages = UsageScanner.scan(scripts, emptyMap(), "Run `composer test` before submitting a PR.")
        assertEquals(UsageVerdict.USED_IN_README, usages[0].verdict)
    }

    @Test
    fun `a Composer script event name is LIFECYCLE_HOOK even with zero references`() {
        val scripts = listOf(script("post-install-cmd", "echo done"))
        val usages = UsageScanner.scan(scripts, emptyMap(), null)
        assertEquals(UsageVerdict.LIFECYCLE_HOOK, usages[0].verdict)
    }

    @Test
    fun `a script with no reference anywhere is ORPHANED`() {
        val scripts = listOf(script("old-migration", "php migrate.php"))
        val usages = UsageScanner.scan(scripts, emptyMap(), null)
        assertEquals(UsageVerdict.ORPHANED, usages[0].verdict)
    }
}
