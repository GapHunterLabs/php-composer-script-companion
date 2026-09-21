package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.openapi.vfs.VirtualFile

/**
 * Every project file that may invoke a Composer script, next to the
 * composer.json being checked. Chosen from a corpus of 200 real PHP
 * repositories (1,579 custom scripts): of the scripts invoked nowhere the
 * plugin used to look (GitHub Actions, GitLab CI, CircleCI, the root README),
 * the invocation was in CONTRIBUTING.md for 41, a Makefile or package.json
 * for 10 each, captainhook.json for 6, and otherwise in shell scripts under
 * bin/ or a README one folder down. Two more passes, searching whole
 * repositories for random samples of the remaining orphan verdicts, found
 * the rest in AGENTS.md (instructions for coding agents), Markdown files
 * under docs/ (up to two folders down), and a lowercase readme.md. The
 * other CI systems and Docker files are cheap fixed paths, so they're read
 * too.
 *
 * Fixed paths and shallow, capped folder listings only: never a walk of the
 * whole project, so the cost doesn't grow with its size. File names are
 * matched ignoring case (readme.md, makefile), as they are on Windows and
 * macOS anyway.
 */
object CiFileLocator {

    private val FIXED_PATHS = listOf(
        // CI
        ".gitlab-ci.yml", ".circleci/config.yml", ".travis.yml", "bitbucket-pipelines.yml",
        "Jenkinsfile", "azure-pipelines.yml",
        // build, containers, git hooks
        "Makefile", "package.json", "captainhook.json", "grumphp.yml", "grumphp.yml.dist",
        "Dockerfile", "docker-compose.yml", "docker-compose.yaml", "compose.yml", "compose.yaml",
        // contributor docs (the root README is read separately, see findReadme),
        // including the instructions projects now write for coding agents
        "CONTRIBUTING.md", ".github/CONTRIBUTING.md",
        "AGENTS.md", "CLAUDE.md", ".github/copilot-instructions.md",
    )

    /** Every file with one of these extensions directly inside the folder. */
    private val FOLDERS = listOf(
        ".github/workflows" to setOf("yml", "yaml"),
        "" to setOf("sh"),
        "bin" to setOf("sh"),
        "scripts" to setOf("sh"),
        "build" to setOf("sh"),
    )

    private val DOCS_FOLDERS = listOf("docs", "doc")
    private const val DOCS_DEPTH = 2
    private const val MAX_DOCS_FILES = 200
    private const val MAX_SUBFOLDER_READMES = 40

    fun findCiFiles(composerJsonDir: VirtualFile): List<VirtualFile> {
        val results = LinkedHashSet<VirtualFile>()

        for (path in FIXED_PATHS) {
            findIgnoringCase(composerJsonDir, path)?.takeIf { !it.isDirectory }?.let(results::add)
        }

        for ((folder, extensions) in FOLDERS) {
            val dir = if (folder.isEmpty()) composerJsonDir else findIgnoringCase(composerJsonDir, folder)
            dir?.takeIf { it.isDirectory }?.children
                ?.filter { !it.isDirectory && it.extension in extensions }
                ?.let(results::addAll)
        }

        // Composite actions: .github/actions/<name>/action.yml
        composerJsonDir.findFileByRelativePath(".github/actions")?.takeIf { it.isDirectory }?.children?.forEach { action ->
            (action.findChild("action.yml") ?: action.findChild("action.yaml"))?.takeIf { !it.isDirectory }?.let(results::add)
        }

        // Markdown under docs/ (docs/DEVELOPER.md, docs/meta/contributing.md...).
        val docs = mutableListOf<VirtualFile>()
        for (folder in DOCS_FOLDERS) {
            findIgnoringCase(composerJsonDir, folder)?.takeIf { it.isDirectory }?.let { collectMarkdown(it, DOCS_DEPTH, docs) }
        }
        results.addAll(docs)

        // READMEs one folder down (scripts/README.md, tests/README.md...).
        composerJsonDir.children
            .filter { it.isDirectory && it.name != "vendor" && it.name != "node_modules" && !it.name.startsWith(".") }
            .mapNotNull { findIgnoringCase(it, "README.md")?.takeIf { readme -> !readme.isDirectory } }
            .take(MAX_SUBFOLDER_READMES)
            .let(results::addAll)

        return results.toList()
    }

    fun findReadme(composerJsonDir: VirtualFile): VirtualFile? =
        findIgnoringCase(composerJsonDir, "README.md")?.takeIf { !it.isDirectory }

    private fun collectMarkdown(dir: VirtualFile, depth: Int, into: MutableList<VirtualFile>) {
        for (child in dir.children) {
            if (into.size >= MAX_DOCS_FILES) return
            when {
                child.isDirectory -> if (depth > 1) collectMarkdown(child, depth - 1, into)
                child.extension.equals("md", ignoreCase = true) -> into.add(child)
            }
        }
    }

    /** [relativePath] under [dir], its last segment matched ignoring case. */
    private fun findIgnoringCase(dir: VirtualFile, relativePath: String): VirtualFile? {
        dir.findFileByRelativePath(relativePath)?.let { return it }
        val parentPath = relativePath.substringBeforeLast('/', "")
        val name = relativePath.substringAfterLast('/')
        val parent = if (parentPath.isEmpty()) dir else dir.findFileByRelativePath(parentPath) ?: return null
        return parent.children.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
