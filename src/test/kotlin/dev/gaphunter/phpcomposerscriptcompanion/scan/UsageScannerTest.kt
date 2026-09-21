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
    fun `a script reused with Composer's @name is USED_IN_OWN_COMPOSER_JSON`() {
        // Composer's documented way to reuse a script; the most common one on real projects.
        val scripts = listOf(script("test", "@lint\n@phpunit"), script("lint", "phpcs"), script("phpunit", "phpunit"))
        val usages = UsageScanner.scan(scripts, emptyMap(), null).associateBy { it.script.name }
        assertEquals(UsageVerdict.USED_IN_OWN_COMPOSER_JSON, usages.getValue("lint").verdict)
        assertEquals(UsageVerdict.USED_IN_OWN_COMPOSER_JSON, usages.getValue("phpunit").verdict)
    }

    @Test
    fun `a script run through one of its scripts-aliases is used`() {
        val phpstan = ComposerScript("phpstan", "phpstan analyse", 0, aliases = listOf("stan"))
        val usages = UsageScanner.scan(listOf(phpstan), mapOf("Makefile" to "lint:\n\tcomposer stan"), null)
        assertEquals(UsageVerdict.USED_IN_CI, usages[0].verdict)
    }

    @Test
    fun `a script picked from a CI matrix by a dynamic invocation is not orphaned`() {
        // Real shape (a public repo's workflow): run: composer ${{ matrix.command }}
        val workflow = "matrix:\n  include:\n    - command: phpstan\n    - command: smoke\nsteps:\n  - run: composer \${{ matrix.command }}"
        val scripts = listOf(script("phpstan", "phpstan"), script("smoke", "php smoke.php"), script("legacy", "php old.php"))
        val usages = UsageScanner.scan(scripts, mapOf(".github/workflows/ci.yml" to workflow), null).associateBy { it.script.name }
        assertEquals(UsageVerdict.USED_IN_CI, usages.getValue("phpstan").verdict)
        assertEquals(UsageVerdict.USED_IN_CI, usages.getValue("smoke").verdict)
        assertEquals(UsageVerdict.ORPHANED, usages.getValue("legacy").verdict)
    }

    @Test
    fun `a name that merely appears in a file without any composer call proves nothing`() {
        val usages = UsageScanner.scan(listOf(script("phpstan", "phpstan")), mapOf("ci.yml" to "run: vendor/bin/phpstan analyse"), null)
        assertEquals(UsageVerdict.ORPHANED, usages[0].verdict)
    }

    @Test
    fun `a script with no reference anywhere is ORPHANED`() {
        val scripts = listOf(script("old-migration", "php migrate.php"))
        val usages = UsageScanner.scan(scripts, emptyMap(), null)
        assertEquals(UsageVerdict.ORPHANED, usages[0].verdict)
    }
}
