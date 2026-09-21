package dev.gaphunter.phpcomposerscriptcompanion.scan

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import dev.gaphunter.phpcomposerscriptcompanion.model.ComposerScript
import dev.gaphunter.phpcomposerscriptcompanion.model.ScriptUsage
import dev.gaphunter.phpcomposerscriptcompanion.parse.ComposerJsonParser
import java.nio.charset.StandardCharsets

/**
 * The whole check for one `composer.json`: its scripts, every project file
 * that may invoke one, and the verdict per script. Shared by the gutter and
 * the tests, so both run exactly the same code.
 */
object ComposerScriptAudit {

    fun audit(jsonFile: JsonFile): List<ScriptUsage> {
        val scripts = ComposerJsonParser.parseScripts(jsonFile)
        if (scripts.isEmpty()) return emptyList()
        return audit(scripts, jsonFile.virtualFile?.parent)
    }

    fun audit(scripts: List<ComposerScript>, dir: VirtualFile?): List<ScriptUsage> {
        val sourceFiles: Map<String, String> = dir?.let { root ->
            CiFileLocator.findCiFiles(root).associate { file ->
                ProgressManager.checkCanceled()
                relativeLabel(root, file) to readTextSafely(file)
            }
        }.orEmpty()
        val readmeText = dir?.let { CiFileLocator.findReadme(it) }?.let { readTextSafely(it) }
        return UsageScanner.scan(scripts, sourceFiles, readmeText)
    }

    private fun relativeLabel(dir: VirtualFile, file: VirtualFile): String {
        val dirPath = dir.path
        val filePath = file.path
        return if (filePath.startsWith(dirPath)) filePath.removePrefix(dirPath).trimStart('/', '\\') else file.name
    }

    private fun readTextSafely(file: VirtualFile): String = try {
        if (file.length > MAX_FILE_BYTES) "" else String(file.contentsToByteArray(), StandardCharsets.UTF_8)
    } catch (_: Exception) {
        ""
    }

    /** A build or CI file bigger than this is generated or vendored, never hand-written invocations. */
    private const val MAX_FILE_BYTES = 512 * 1024
}
