package com.satotats

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns.string
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.idea.completion.or
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

class CustomGradlePluginContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val patterns: PsiElementPattern.Capture<GrLiteral> =
            PlatformPatterns.psiElement(GrLiteral::class.java)
                .inFile(
                    PlatformPatterns.psiFile()
                        .withName(string().endsWith(".gradle").or(string().endsWith(".gradle.kts")))
                )
        registrar.registerReferenceProvider(
            patterns,
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    println(element.text)
                    val pluginName = element.text.trim('"')
                    val pluginFile = FilenameIndex.getFilesByName(
                        element.project,
                        withExtension(pluginName),
                        GlobalSearchScope.projectScope(element.project)
                    ).firstOrNull()
                    return if (pluginFile != null) arrayOf(pluginFile.reference!!) else arrayOf()
                }
            }
        )
    }

    private fun withExtension(pluginName: String): String {
        return "$pluginName.gradle"
    }

}
