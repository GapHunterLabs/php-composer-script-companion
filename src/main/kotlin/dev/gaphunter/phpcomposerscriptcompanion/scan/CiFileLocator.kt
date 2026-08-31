package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.openapi.vfs.VirtualFile

/** v0.1/v0.2 CI scope: GitHub Actions, GitLab CI, and CircleCI -- Jenkinsfile/Azure Pipelines remain a real, documented gap, not a hidden one. */
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

        // CircleCI: a single fixed path, unlike GitHub Actions' whole
        // directory of arbitrarily-named workflow files -- no wildcard
        // scan needed.
        val circleCi = composerJsonDir.findChild(".circleci")?.findChild("config.yml")
        if (circleCi != null && !circleCi.isDirectory) {
            results.add(circleCi)
        }

        return results
    }

    fun findReadme(composerJsonDir: VirtualFile): VirtualFile? {
        val readme = composerJsonDir.findChild("README.md")
        return if (readme != null && !readme.isDirectory) readme else null
    }
}
