package com.satotats.intellij.plugins.gradle

import com.intellij.model.Pointer
import com.intellij.model.presentation.PresentableSymbol
import com.intellij.model.presentation.SymbolPresentation
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icons.GradleIcons
import org.jetbrains.plugins.gradle.util.GradleBundle

class GradlePluginSymbol(
    private val psiFile: PsiFile,
) : PresentableSymbol,
    NavigatableSymbol {

    private val pluginFileName = psiFile.name

    override fun createPointer() = Pointer.hardPointer(this)

    private val myPresentation = SymbolPresentation.create(
        GradleIcons.Gradle,
        pluginFileName,
        GradleBundle.message("gradle.project.0", pluginFileName)
    )

    override fun getSymbolPresentation() = myPresentation

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
        return listOf(SymbolNavigationService.getInstance().psiFileNavigationTarget(psiFile))
    }
}