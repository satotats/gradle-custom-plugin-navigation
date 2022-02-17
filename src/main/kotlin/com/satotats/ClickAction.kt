package com.satotats

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.nextLeaf
import com.intellij.psi.util.prevLeaf


class ClickAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val fileName = file?.name!!

        if (!fileName.endsWith(".gradle") && !fileName.endsWith(".gradle.kts")) return
        val isKotlinScript = isKotlinScript(fileName)

        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor != null) {
            val elementClicked = file.findElementAt(editor.caretModel.offset)!!
            if (isElementQuoted(elementClicked)) {
                val nestDepth = findPluginDeclaration(elementClicked)
                if (isCaretInPluginsScope(nestDepth)) {
                    val project = e.project!!
                    val pluginName = elementClicked.text.trim('"')

                    val pluginFile = FilenameIndex.getFilesByName(
                        project,
                        withExtension(pluginName, isKotlinScript),
                        GlobalSearchScope.projectScope(project)
                    ).firstOrNull() // only a single result is supported as of now

                    if (pluginFile != null) {
                        // move to the file found
                        FileEditorManager.getInstance(project)
                            .openTextEditor(
                                OpenFileDescriptor(
                                    project,
                                    pluginFile.virtualFile
                                ),
                                true
                            )
                    }
                }
            }
        }
    }

    private fun isKotlinScript(fileName: String): Boolean {
        return fileName.endsWith(".gradle.kts")
    }

    private fun withExtension(pluginName: String, isKotlinScript: Boolean): String {
        return if (isKotlinScript) "$pluginName.gradle.kts" else "$pluginName.gradle"
    }

    private fun isElementQuoted(element: PsiElement): Boolean {
        return isQuotedStr(element.text)
                || (element.prevLeaf(true)?.text == "\""
                && element.nextLeaf(true)?.text == "\"")
    }

    /** for .gradle(Groovy) file */
    private fun isQuotedStr(str: String): Boolean {
        return str.startsWith("\"") && str.endsWith("\"")
    }

    /** @return nestDepth */
    private fun findPluginDeclaration(leaf: PsiElement) = findPluginDeclaration(0, leaf)


    private fun findPluginDeclaration(nestDepth: Int, leaf: PsiElement): Int {
        val prevLeaf = leaf.prevLeaf(skipEmptyElements = true)
        return when (prevLeaf?.text) {
            "{" -> findPluginDeclaration(nestDepth - 1, prevLeaf)
            "}" -> findPluginDeclaration(nestDepth + 1, prevLeaf)
            "plugins" -> nestDepth
            null -> ABORTED // abort
            else -> findPluginDeclaration(nestDepth, prevLeaf)
        }
    }

    /** if the caret is in plugins{ ... } scope, the nestDepth will be negative */
    private fun isCaretInPluginsScope(nestDepth: Int) = nestDepth < 0

    companion object {
        const val ABORTED = 999
    }
}