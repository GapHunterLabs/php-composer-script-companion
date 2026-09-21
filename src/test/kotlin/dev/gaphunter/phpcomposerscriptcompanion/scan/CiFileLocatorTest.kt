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

    fun `test the build files, hooks and contributor docs where real projects invoke scripts are found`() {
        val expected = listOf(
            "CONTRIBUTING.md", ".github/CONTRIBUTING.md", "Makefile", "package.json", "captainhook.json",
            "grumphp.yml.dist", ".travis.yml", "bitbucket-pipelines.yml", "Jenkinsfile", "azure-pipelines.yml",
            "Dockerfile", "docker-compose.yml", "compose.yaml",
            "build.sh", "bin/build.sh", "scripts/ci.sh", ".github/actions/setup/action.yml", "tests/README.md",
            "AGENTS.md", "CLAUDE.md", ".github/copilot-instructions.md", "docs/DEVELOPER.md", "doc/release.md",
            "docs/meta/contributing.md",
        )
        expected.forEach { myFixture.addFileToProject(it, "composer test") }
        val dir = composerJsonDir()
        val found = CiFileLocator.findCiFiles(dir).map { it.path.removePrefix(dir.path + "/") }.toSet()
        assertEquals(expected.toSet(), found)
    }

    fun `test files that never run scripts are not read`() {
        listOf("src/Foo.php", "bin/console", "vendor/acme/pkg/README.md", "docs/a/b/too-deep.md", ".idea/README.md")
            .forEach { myFixture.addFileToProject(it, "composer test") }
        assertEquals(emptyList<String>(), CiFileLocator.findCiFiles(composerJsonDir()).map { it.path })
    }

    fun `test fixed names are matched ignoring case`() {
        // Real case on a public repo: a lowercase readme.md held the only invocation.
        myFixture.addFileToProject("readme.md", "Run composer fix:changed")
        myFixture.addFileToProject("makefile", "lint:\n\tcomposer lint")
        val dir = composerJsonDir()
        assertEquals("readme.md", CiFileLocator.findReadme(dir)?.name)
        assertTrue(CiFileLocator.findCiFiles(dir).any { it.name == "makefile" })
    }
}
