package com.satotats.intellij.plugins.gradle

import com.intellij.model.Pointer
import com.intellij.model.presentation.PresentableSymbol
import com.intellij.model.presentation.SymbolPresentation
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import icons.GradleIcons
import org.jetbrains.plugins.gradle.util.GradleBundle

class GradlePluginSymbol(
    private val pluginName: String,
) : PresentableSymbol,
    NavigatableSymbol {

    override fun createPointer() = Pointer.hardPointer(this)

    private val myPresentation = SymbolPresentation.create(
        GradleIcons.Gradle,
        pluginName,
        GradleBundle.message("gradle.project.0", pluginName)
    )

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
        val pluginFile = FilenameIndex.getFilesByName(
            project,
            pluginName,
            GlobalSearchScope.projectScope(project)
        ).firstOrNull() ?: return emptyList()
        return listOf(SymbolNavigationService.getInstance().psiFileNavigationTarget(pluginFile))
    }

    override fun getSymbolPresentation() = myPresentation
}