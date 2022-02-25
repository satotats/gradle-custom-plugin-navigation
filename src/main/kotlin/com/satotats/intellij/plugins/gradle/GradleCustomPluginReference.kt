package com.satotats.intellij.plugins.gradle

import com.intellij.model.SingleTargetReference
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

class GradleCustomPluginReference(
    private val myElement: GrLiteral,
    private val fileFound: PsiFile,
) : SingleTargetReference(), PsiSymbolReference {

    override fun resolveSingleTarget(): Symbol? {
        return GradleCustomPluginSymbol(fileFound)
    }

    override fun getElement() = myElement

    override fun getRangeInElement() = TextRange(1, myElement.text.length - 1)
}