package com.satotats

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.model.Pointer
import com.intellij.model.presentation.PresentableSymbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.SymbolNavigationService
import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache

class GradlePluginSymbol(
    val rootProjectPath: String,
    val projectName: String,
    val qualifiedName: String,
) : PresentableSymbol,
    NavigatableSymbol,
    SearchTarget {

    override fun createPointer(): Pointer<out GradlePluginSymbol> = Pointer.hardPointer(this)

    final override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
        val psiFile = findBuildFile(project)
            ?: return emptyList()
        return listOf(SymbolNavigationService.getInstance().psiFileNavigationTarget(psiFile))
    }

    private fun findBuildFile(project: Project): PsiFile? {
        val rootProject =
            ExternalProjectDataCache.getInstance(project).getRootExternalProject(rootProjectPath) ?: return null
//        val buildFile = externalProject(rootProject)?.buildFile ?: return null
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(buildFile) ?: return null
        return PsiManager.getInstance(project).findFile(virtualFile)
    }

//    protected abstract fun externalProject(rootProject: ExternalProject): ExternalProject?

    override val usageHandler: UsageHandler<*> get() = UsageHandler.createEmptyUsageHandler(projectName)

    override val presentation: TargetPresentation
        get() {
            val presentation = symbolPresentation
            return TargetPresentation
                .builder(presentation.longDescription)
                .icon(presentation.icon)
                .presentation()
        }
}