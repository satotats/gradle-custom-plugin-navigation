package com.satotats.intellij.plugins.gradle

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.patterns.GroovyPatterns
import org.jetbrains.plugins.groovy.lang.psi.patterns.groovyElement

class GradleCustomPluginReferenceProvider : PsiSymbolReferenceProvider {
    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): Collection<PsiSymbolReference> {
        if (element !is GrLiteral) return emptyList()

        // proceed if the file is ".gradle" file
        val fileName = element.containingFile.originalFile.name
        if (!fileName.endsWith(".gradle")) return emptyList()

        if (!pluginApplicationPattern.accepts(element)) return emptyList()
        return listOf(GradlePluginReference(element))
    }

    override fun getSearchRequests(project: Project, target: Symbol): Collection<SearchRequest> = emptyList()

    companion object {
        val pluginApplicationPattern = GroovyPatterns.stringLiteral().withParent(
            groovyElement<GrArgumentList>()
                .withParent(
                    groovyElement<GrMethodCall>()
                        .withText(GroovyPatterns.string().startsWith("id"))
                )
        )
    }
}