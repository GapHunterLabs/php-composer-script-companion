package dev.gaphunter.phpcomposerscriptcompanion.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptReferenceMatcherTest {

    @Test
    fun `composer run-script name is matched`() {
        assertTrue(ScriptReferenceMatcher.isReferenced("composer run-script test", "test"))
    }

    @Test
    fun `bare composer name is matched`() {
        assertTrue(ScriptReferenceMatcher.isReferenced("composer test", "test"))
    }

    @Test
    fun `a longer script name sharing a prefix is not falsely matched`() {
        assertFalse(ScriptReferenceMatcher.isReferenced("composer test-integration", "test"))
    }

    @Test
    fun `unrelated text mentioning the word composer is not a match`() {
        assertFalse(ScriptReferenceMatcher.isReferenced("This project uses Composer for dependency management.", "test"))
    }

    @Test
    fun `the name appearing without the composer keyword at all is not matched`() {
        assertFalse(ScriptReferenceMatcher.isReferenced("Run the test suite before merging.", "test"))
    }

    @Test
    fun `every invocation shape Composer documents is matched`() {
        listOf(
            "composer run test",                                   // run is run-script's alias
            "composer run-script --timeout=0 test",
            "composer run-script --timeout 0 test",
            "composer -n run test",
            "composer --no-interaction --working-dir=app test",
            "composer -d app test",
            "php composer.phar run-script test",
            "./composer.phar test",
            "bin/composer test",
            "@composer run test",
            "COMPOSER_MEMORY_LIMIT=-1 composer test -- --filter Foo",
        ).forEach { assertTrue(it, ScriptReferenceMatcher.isReferenced(it, "test")) }
    }

    @Test
    fun `script names with colons are matched whole`() {
        assertTrue(ScriptReferenceMatcher.isReferenced("composer test:unit", "test:unit"))
        assertFalse(ScriptReferenceMatcher.isReferenced("composer test:unit", "test"))
        assertFalse(ScriptReferenceMatcher.isReferenced("composer test", "test:unit"))
    }

    @Test
    fun `lookalike tools are not composer`() {
        assertFalse(ScriptReferenceMatcher.isReferenced("composer-normalize test", "test"))
        assertFalse(ScriptReferenceMatcher.isReferenced("mycomposer test", "test"))
    }

    @Test
    fun `at-references count only from inside composer json scripts`() {
        assertTrue(ScriptReferenceMatcher.isReferencedFromScript("@lint", "lint"))
        assertTrue(ScriptReferenceMatcher.isReferencedFromScript("@lint --fix", "lint"))
        assertFalse(ScriptReferenceMatcher.isReferencedFromScript("@lint:strict", "lint"))
        assertFalse(ScriptReferenceMatcher.isReferencedFromScript("email@lint", "lint"))
        assertFalse(ScriptReferenceMatcher.isReferenced("@lint", "lint"))
    }

    @Test
    fun `a name counts next to a dynamic call only when written as a value`() {
        assertTrue(ScriptReferenceMatcher.mentionsName("task: [phpstan, smoke]", "phpstan"))
        assertTrue(ScriptReferenceMatcher.mentionsName("task: [phpstan, smoke]", "smoke"))
        assertTrue(ScriptReferenceMatcher.mentionsName("  - command: phpstan\n", "phpstan"))
        assertTrue(ScriptReferenceMatcher.mentionsName("matrix:\n  - smoke\n", "smoke"))
        assertTrue(ScriptReferenceMatcher.mentionsName("echo \"export GRUMPHP_COMMAND=grumphp_complete\" >> \$BASH_ENV", "grumphp_complete"))
        assertFalse(ScriptReferenceMatcher.mentionsName("run: vendor/bin/php-cs-fixer fix --dry-run", "fix"))
        assertFalse(ScriptReferenceMatcher.mentionsName("task: [phpstan-baseline]", "phpstan"))
        assertFalse(ScriptReferenceMatcher.mentionsName("      # not the 'fix' script: the tool itself", "fix"))
    }

    @Test
    fun `dynamic invocations are recognized`() {
        assertTrue(ScriptReferenceMatcher.hasDynamicInvocation("run: composer \${{ matrix.command }}"))
        assertTrue(ScriptReferenceMatcher.hasDynamicInvocation("composer run \${GRUMPHP_COMMAND}"))
        assertTrue(ScriptReferenceMatcher.hasDynamicInvocation("composer run-script \$TASK"))
        assertTrue(ScriptReferenceMatcher.hasDynamicInvocation("docker compose run app composer \$(ARGS)"))
        assertFalse(ScriptReferenceMatcher.hasDynamicInvocation("composer install --no-progress"))
    }
}
