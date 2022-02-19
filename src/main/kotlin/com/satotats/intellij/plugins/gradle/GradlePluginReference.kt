package com.satotats.intellij.plugins.gradle

import com.intellij.model.SingleTargetReference
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

class GradlePluginReference(
    private val myElement: GrLiteral,
) : SingleTargetReference(), PsiSymbolReference {

    private val pluginName = myElement.text.trim('\"', '\'')

    override fun resolveSingleTarget(): Symbol? {
        val pluginFileName = "$pluginName${GradleExt.Groovy}"
        return GradlePluginSymbol(pluginFileName)
    }

    override fun getElement() = myElement

    override fun getRangeInElement() = TextRange(0, pluginName.length)

    private object GradleExt {
        const val Kotlin = ".gradle.kts"
        const val Groovy = ".gradle"
    }
}