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
}
