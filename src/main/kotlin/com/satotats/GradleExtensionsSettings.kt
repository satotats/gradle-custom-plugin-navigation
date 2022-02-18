// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.gradle.settings

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiElement
import com.intellij.util.SmartList
import org.jetbrains.annotations.Contract
import org.jetbrains.plugins.gradle.config.GradleSettingsListenerAdapter
import org.jetbrains.plugins.gradle.model.GradleExtensions
import org.jetbrains.plugins.gradle.service.project.GradleProjectResolverUtil
import org.jetbrains.plugins.gradle.service.resolve.GradleCommonClassNames
import org.jetbrains.plugins.gradle.util.GradleConstants

/**
 * @author Vladislav.Soroka
 */
class GradleExtensionsSettings(project: Project?) {
    private val myState = Settings()

    class Settings {
        var projects: Map<String, GradleProject> = HashMap()
        fun add(
            rootPath: String,
            extensionsData: Collection<DataNode<GradleExtensions?>?>
        ) {
            val extensionMap: MutableMap<String, GradleExtensions> = HashMap()
            for (node in extensionsData) {
                val parent = node!!.parent ?: continue
                if (parent.data !is ModuleData) continue
                val gradlePath = GradleProjectResolverUtil.getGradlePath((parent.data as ModuleData))
                extensionMap[gradlePath] = node.getData()
            }
            add(rootPath, extensionMap)
        }

        fun add(rootPath: String, extensions: Map<String, GradleExtensions>) {
            val gradleProject = GradleProject()
            for ((key, gradleExtensions) in extensions) {
                val extensionsData = GradleExtensionsData()
                extensionsData.parent = gradleExtensions.parentProjectPath
                for (extension in gradleExtensions.extensions) {
                    val gradleExtension = GradleExtension()
                    gradleExtension.name = extension.name
                    gradleExtension.typeFqn = extension.typeFqn
                    extensionsData.extensions[extension.name] = gradleExtension
                }
                for (convention in gradleExtensions.conventions) {
                    val gradleConvention = GradleConvention()
                    gradleConvention.name = convention.name
                    gradleConvention.typeFqn = convention.typeFqn
                    extensionsData.conventions.add(gradleConvention)
                }
                for (property in gradleExtensions.gradleProperties) {
                    val gradleProp = GradleProp()
                    gradleProp.name = property.name
                    gradleProp.typeFqn = property.typeFqn
                    extensionsData.properties[gradleProp.name] = gradleProp
                }
                for (task in gradleExtensions.tasks) {
                    val gradleTask = GradleTask()
                    gradleTask.name = task.name
                    val type = task.type
                    if (type != null) {
                        gradleTask.typeFqn = type
                    }
                    val description = StringBuilder()
                    if (task.description != null) {
                        description.append(task.description)
                        if (task.group != null) {
                            description.append("<p>")
                        }
                    }
                    if (task.group != null) {
                        description.append("<i>Task group: ").append(task.group).append("<i>")
                    }
                    gradleTask.description = description.toString()
                    extensionsData.tasksMap[gradleTask.name] = gradleTask
                }
                for (configuration in gradleExtensions.configurations) {
                    val gradleConfiguration = GradleConfiguration()
                    gradleConfiguration.name = configuration.name
                    gradleConfiguration.description = configuration.description
                    gradleConfiguration.visible = configuration.isVisible
                    gradleConfiguration.scriptClasspath = configuration.isScriptClasspathConfiguration
                    if (gradleConfiguration.scriptClasspath) {
                        extensionsData.buildScriptConfigurations[configuration.name] = gradleConfiguration
                    } else {
                        extensionsData.configurations[configuration.name] = gradleConfiguration
                    }
                }
                gradleProject.extensions[key] = extensionsData
                extensionsData.myGradleProject = gradleProject
            }
            val projects: MutableMap<String, GradleProject> = HashMap(
                projects
            )
            projects[rootPath] = gradleProject
            this.projects = projects
        }

        fun remove(rootPaths: Set<String>) {
            val projects: MutableMap<String, GradleProject> = HashMap(projects)
            for (path in rootPaths) {
                projects.remove(path)
            }
            this.projects = projects
        }

        /**
         * Returns extensions available in the context of the gradle project related to the IDE module.
         */
        fun getExtensionsFor(module: Module?): GradleExtensionsData? {
            return if (module == null) null else getExtensionsFor(
                ExternalSystemApiUtil.getExternalRootProjectPath(module),
                GradleProjectResolverUtil.getGradlePath(module)
            )
        }

        /**
         * Returns extensions available in the context of the specified (using gradle path notation, e.g. `:sub-project`) gradle project.
         *
         * @param rootProjectPath file path of the root gradle project
         * @param gradlePath      gradle project path notation
         * @return gradle extensions
         */
        fun getExtensionsFor(rootProjectPath: String?, gradlePath: String?): GradleExtensionsData? {
            val gradleProject = getRootGradleProject(rootProjectPath) ?: return null
            return gradleProject.extensions[gradlePath]
        }

        @Contract("null -> null")
        fun getRootGradleProject(rootProjectPath: String?): GradleProject? {
            return if (rootProjectPath == null) null else projects[rootProjectPath]
        }
    }

    class GradleProject {
        var extensions: MutableMap<String, GradleExtensionsData> = HashMap()
    }

    class GradleExtensionsData {
        var myGradleProject: GradleProject? = null
        var parent: String? = null
        val extensions: MutableMap<String, GradleExtension> = HashMap()
        val conventions: MutableList<GradleConvention> = SmartList()
        val properties: MutableMap<String?, GradleProp> = HashMap()
        val tasksMap: MutableMap<String?, GradleTask> = LinkedHashMap()
        val configurations: MutableMap<String, GradleConfiguration> = HashMap()
        val buildScriptConfigurations: MutableMap<String, GradleConfiguration> = HashMap()
        fun getParent(): GradleExtensionsData? {
            return myGradleProject?.extensions?.get(parent)
        }

        fun findProperty(name: String?): GradleProp? {
            return findProperty(this, name)
        }

        fun findAllProperties(): Collection<GradleProp> {
            return findAllProperties(this, HashMap())
        }

        companion object {
            private fun findAllProperties(
                extensionsData: GradleExtensionsData,
                result: MutableMap<String?, GradleProp>
            ): Collection<GradleProp> {
                for (property in extensionsData.properties.values) {
                    result.putIfAbsent(property.name, property)
                }
                if (extensionsData.getParent() != null) {
                    findAllProperties(
                        extensionsData.getParent()!!, result
                    )
                }
                return result.values
            }

            private fun findProperty(extensionsData: GradleExtensionsData, propName: String?): GradleProp? {
                val prop = extensionsData.properties[propName]
                if (prop != null) return prop
                if (extensionsData.parent != null && extensionsData.myGradleProject != null) {
                    val parentData = extensionsData.myGradleProject?.extensions?.get(extensionsData.parent!!)
                    if (parentData != null) {
                        return findProperty(parentData, propName)
                    }
                }
                return null
            }
        }
    }

    interface TypeAware {
        val typeFqn: String?
    }

    class GradleExtension : TypeAware {
        var name: String? = null
        override var typeFqn = CommonClassNames.JAVA_LANG_OBJECT_SHORT
    }

    class GradleConvention : TypeAware {
        var name: String? = null
        override var typeFqn = CommonClassNames.JAVA_LANG_OBJECT_SHORT
    }

    class GradleProp : TypeAware {
        var name: String? = null
        override var typeFqn = CommonClassNames.JAVA_LANG_STRING
        var value: String? = null
    }

    class GradleTask : TypeAware {
        var name: String? = null
        override var typeFqn = GradleCommonClassNames.GRADLE_API_DEFAULT_TASK
        var description: String? = null
    }

    class GradleConfiguration {
        var name: String? = null
        var visible = true
        var scriptClasspath = false
        var description: String? = null
    }

    companion object {
        private val LOG = Logger.getInstance(
            GradleExtensionsSettings::class.java
        )

        fun getInstance(project: Project): Settings {
            return project.getService(GradleExtensionsSettings::class.java).myState
        }

//        fun load(project: Project) {
//            val projectsData =
//                ProjectDataManager.getInstance().getExternalProjectsData(project, GradleConstants.SYSTEM_ID)
//            for (projectInfo in projectsData) {
//                val projectDataNode = projectInfo.externalProjectStructure ?: continue
//                val projectPath = projectInfo.externalProjectPath
//                try {
//                    val nodes: Collection<DataNode<GradleExtensions?>?> = SmartList()
//                    for (moduleNode in ExternalSystemApiUtil.findAll(projectDataNode, ProjectKeys.MODULE)) {
//                        ContainerUtil.addIfNotNull<DataNode<GradleExtensions?>?>(
//                            nodes, ExternalSystemApiUtil.find(
//                                moduleNode!!, GradleExtensionsDataService.KEY
//                            )
//                        )
//                    }
//                    getInstance(project).add(projectPath, nodes)
//                } catch (e: ClassCastException) {
//                    // catch deserialization issue caused by fast serializer
//                    LOG.debug(e)
//                    ExternalProjectsManager.getInstance(project).externalProjectsWatcher.markDirty(projectPath)
//                }
//            }
//        }

        fun getRootProject(element: PsiElement): GradleProject? {
            val containingFile = element.containingFile.originalFile
            val project = containingFile.project
            return getInstance(project).getRootGradleProject(getRootProjectPath(element))
        }

        fun getRootProjectPath(element: PsiElement): String? {
            val containingFile = element.containingFile.originalFile
            val module = ModuleUtilCore.findModuleForFile(containingFile)
            return ExternalSystemApiUtil.getExternalRootProjectPath(module)
        }
    }

    init {
        ExternalSystemApiUtil.subscribe(project!!, GradleConstants.SYSTEM_ID, object : GradleSettingsListenerAdapter() {
            override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) {
                myState.remove(linkedProjectPaths)
            }
        })
    }
}