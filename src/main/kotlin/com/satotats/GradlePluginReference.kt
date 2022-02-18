package com.satotats

import com.intellij.model.SingleTargetReference
import com.intellij.model.Symbol
import org.jetbrains.plugins.gradle.settings.GradleExtensionsSettings
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

class GradlePluginReference(
    private val myElement: GrLiteral,
) : SingleTargetReference() {
    override fun resolveSingleTarget(): Symbol? {
        val gradleProject = GradleExtensionsSettings.getRootProject(myElement) ?: return null
        val rootProjectPath = GradleExtensionsSettings.getRootProjectPath(myElement) ?: return null
        return GradlePluginSymbol(rootProjectPath, "", "")
    }
}