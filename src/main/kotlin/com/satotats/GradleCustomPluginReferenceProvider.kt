package com.satotats

import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider

class GradleCustomPluginReferenceProvider : PsiSymbolReferenceProvider {
    override fun getReferences(
        element: PsiExternalReferenceHost,
        hints: PsiSymbolReferenceHints
    ): MutableCollection<out PsiSymbolReference> {
        TODO("Not yet implemented")
    }
}