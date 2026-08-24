package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.openapi.vfs.VirtualFile

/** Same v0.1 CI scope as `unused-npm-script-companion`'s `CiFileLocator`: GitHub Actions + GitLab CI only, stated honestly. */
object CiFileLocator {

    fun findCiFiles(composerJsonDir: VirtualFile): List<VirtualFile> {
        val results = mutableListOf<VirtualFile>()

        val workflowsDir = composerJsonDir.findChild(".github")?.findChild("workflows")
        if (workflowsDir != null && workflowsDir.isDirectory) {
            for (child in workflowsDir.children) {
                if (!child.isDirectory && (child.extension == "yml" || child.extension == "yaml")) {
                    results.add(child)
                }
            }
        }

        val gitlabCi = composerJsonDir.findChild(".gitlab-ci.yml")
        if (gitlabCi != null && !gitlabCi.isDirectory) {
            results.add(gitlabCi)
        }

        return results
    }

    fun findReadme(composerJsonDir: VirtualFile): VirtualFile? {
        val readme = composerJsonDir.findChild("README.md")
        return if (readme != null && !readme.isDirectory) readme else null
    }
}
