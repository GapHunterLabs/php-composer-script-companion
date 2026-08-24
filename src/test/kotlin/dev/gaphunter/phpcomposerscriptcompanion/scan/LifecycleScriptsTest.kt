package dev.gaphunter.phpcomposerscriptcompanion.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifecycleScriptsTest {

    @Test
    fun `known Composer script event names are always used`() {
        assertTrue(LifecycleScripts.isAlwaysUsed("post-install-cmd", emptySet()))
        assertTrue(LifecycleScripts.isAlwaysUsed("pre-autoload-dump", emptySet()))
        assertTrue(LifecycleScripts.isAlwaysUsed("post-update-cmd", emptySet()))
    }

    @Test
    fun `an unrelated custom script name is not always used`() {
        assertFalse(LifecycleScripts.isAlwaysUsed("lint", emptySet()))
    }

    @Test
    fun `a pre- prefix of a real sibling script is always used`() {
        assertTrue(LifecycleScripts.isAlwaysUsed("pre-lint", setOf("lint")))
    }

    @Test
    fun `a post- prefix with no matching sibling is NOT always used`() {
        assertFalse(LifecycleScripts.isAlwaysUsed("post-nonexistent", setOf("lint")))
    }
}
