package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.gaphunter.phpcomposerscriptcompanion.model.UsageVerdict
import dev.gaphunter.phpcomposerscriptcompanion.parse.ComposerJsonParser
import java.io.File

/**
 * Precision check on real projects, not just hand-written cases: for every
 * `<corpus>/<n>/` (its `composer.json`, plus `repo/` holding that project's
 * files at their real paths) runs the same [ComposerScriptAudit] the gutter
 * uses and writes every verdict to build/corpus-verdicts.txt for manual
 * review. Skipped unless the COMPOSER_SCRIPT_CORPUS environment variable
 * points at a corpus directory (public files from other repositories, never
 * committed here).
 */
class CorpusPrecisionTest : BasePlatformTestCase() {

    fun testVerdictsOnARealCorpus() {
        val corpus = System.getenv("COMPOSER_SCRIPT_CORPUS")?.let(::File)?.takeIf { it.isDirectory } ?: return
        val report = StringBuilder()
        val counts = sortedMapOf<UsageVerdict, Int>()
        for (dir in corpus.listFiles().orEmpty().filter { it.isDirectory }.sortedBy { it.name }) {
            val composerJson = File(dir, "composer.json").takeIf { it.isFile } ?: continue
            val repo = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(dir, "repo")) ?: continue
            // The test sandbox keeps VFS listings between runs: without a
            // recursive refresh, files added to the corpus later stay invisible.
            VfsUtil.markDirtyAndRefresh(false, true, true, repo)
            val source = File(dir, "SOURCE.txt").takeIf { it.isFile }?.readText()?.trim() ?: dir.name
            val json = PsiFileFactory.getInstance(project)
                .createFileFromText("composer.json", JsonFileType.INSTANCE, composerJson.readText()) as JsonFile
            for (usage in ComposerScriptAudit.audit(ComposerJsonParser.parseScripts(json), repo)) {
                counts.merge(usage.verdict, 1, Int::plus)
                report.append(dir.name).append(" | ").append(source).append(" | ").append(usage.script.name)
                    .append(" | ").append(usage.verdict).append(" | ").append(usage.detail).append('\n')
            }
        }
        File("build/corpus-verdicts.txt").writeText("$counts\n$report")
        println("corpus verdicts: $counts -> build/corpus-verdicts.txt")
    }
}
