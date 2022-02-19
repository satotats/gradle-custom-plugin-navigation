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
//        val patterns: PsiElementPattern.Capture<GrLiteral> =
//            PlatformPatterns.psiElement(GrLiteral::class.java)
//                .inFile(
//                    PlatformPatterns.psiFile()
//                        .withName(string().endsWith(".gradle").or(string().endsWith(".gradle.kts")))
//                )
//        registrar.registerReferenceProvider(
//            patterns,
//            GradleCustomPluginReferenceProvider()
//        )
    }
}
