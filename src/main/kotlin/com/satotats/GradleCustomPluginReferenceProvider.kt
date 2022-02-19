package com.satotats

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

class GradleCustomPluginReferenceProvider : PsiSymbolReferenceProvider {
    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): Collection<PsiSymbolReference> =
        if (element is GrLiteral) listOf(GradlePluginReference(element)) else emptyList()

    override fun getSearchRequests(project: Project, target: Symbol): Collection<SearchRequest> = emptyList()
}