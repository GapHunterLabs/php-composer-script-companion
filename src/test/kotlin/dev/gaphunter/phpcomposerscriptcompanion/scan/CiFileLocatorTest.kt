package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CiFileLocatorTest : BasePlatformTestCase() {

    private fun composerJsonDir() = myFixture.addFileToProject(
        "composer.json",
        """{ "scripts": { "test": "phpunit" } }""",
    ).virtualFile.parent

    fun `test finds a GitHub Actions workflow file`() {
        myFixture.addFileToProject(".github/workflows/ci.yml", "run: composer test")
        val found = CiFileLocator.findCiFiles(composerJsonDir())
        assertTrue(found.any { it.name == "ci.yml" })
    }

    fun `test finds a GitLab CI file`() {
        myFixture.addFileToProject(".gitlab-ci.yml", "script: composer test")
        val found = CiFileLocator.findCiFiles(composerJsonDir())
        assertTrue(found.any { it.name == ".gitlab-ci.yml" })
    }

    fun `test finds a CircleCI config file`() {
        myFixture.addFileToProject(".circleci/config.yml", "run: composer test")
        val found = CiFileLocator.findCiFiles(composerJsonDir())
        assertTrue(found.any { it.name == "config.yml" })
    }

    fun `test a project with no CI files at all returns an empty list`() {
        val found = CiFileLocator.findCiFiles(composerJsonDir())
        assertTrue(found.isEmpty())
    }

    fun `test all three CI systems present at once are all found`() {
        myFixture.addFileToProject(".github/workflows/ci.yml", "run: composer test")
        myFixture.addFileToProject(".gitlab-ci.yml", "script: composer test")
        myFixture.addFileToProject(".circleci/config.yml", "run: composer test")
        val found = CiFileLocator.findCiFiles(composerJsonDir())
        assertEquals(3, found.size)
    }
}
