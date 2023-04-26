@file:Suppress("UnstableApiUsage")

package com.redissi.swig.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.ExternalNativeBuildJsonTask
import com.redissi.swig.plugin.extension.JavaWrapper
import com.redissi.swig.plugin.extension.SwigExtension
import com.redissi.swig.plugin.task.GenerateCmakeConfigTask
import com.redissi.swig.plugin.task.GenerateCmakePreloadScriptTask
import com.redissi.swig.plugin.task.GenerateSwigWrapperTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.kotlin.dsl.withType
import java.io.File

public class SwigPlugin : Plugin<Project> {
    internal companion object {
        internal const val GROUP = "swig"
        internal const val EXTENSION_NAME = "swig"
        internal const val SWIG_WRAPPER_TASK_NAME = "generateSwigWrapper"
        internal const val SOURCE_NAME = "swig"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create<SwigExtension>(EXTENSION_NAME)

        val swigWrapperTask = project.tasks.register(SWIG_WRAPPER_TASK_NAME) {
            group = GROUP
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        val dslExtension = DslExtension.Builder(EXTENSION_NAME)
            .extendProjectWith(SwigExtension::class.java)
            .build()

        androidComponents.registerExtension(dslExtension) {
            object : VariantExtension {}
        }

        androidComponents.registerSourceType(SOURCE_NAME)

        androidComponents.onVariants { variant: Variant ->
            extension.javaWrapper.forEach { javaWrapper ->
                configureTasks(project, javaWrapper, variant, swigWrapperTask)
            }
        }
    }

    private fun configureTasks(
        project: Project,
        javaWrapper: JavaWrapper,
        variant: Variant,
        swigWrapperTask: TaskProvider<Task>
    ) {

        val cmakeConfigTask = createOrFindCmakeConfigTask(project, javaWrapper)
        val cmakePreloadTaskName = createOrFindCmakePreloadScriptTask(project, cmakeConfigTask)

        val generateSwigWrapperTask = createGenerateJavaSwigWrapperTask(
            project,
            javaWrapper,
            variant.name,
            swigWrapperTask,
            cmakeConfigTask
        )

        variant.sources.java?.addGeneratedSourceDirectory(generateSwigWrapperTask) { it.outputDir }

        val cmakePreloadFilePath = cmakePreloadTaskName
            .map { File(it.outputDir.get().asFile, it.fileName.get()) }
            .get()
            .absolutePath

        variant.externalNativeBuild?.arguments?.add("-C${cmakePreloadFilePath}")

        project.afterEvaluate {
            tasks.withType(ExternalNativeBuildJsonTask::class).configureEach {
                dependsOn(cmakeConfigTask)
                dependsOn(cmakePreloadTaskName)
            }
        }
    }

    private fun createOrFindCmakeConfigTask(
        project: Project,
        javaWrapper: JavaWrapper
    ): TaskProvider<GenerateCmakeConfigTask> {
        val name = javaWrapper.name
        val nameCapitalized = name.uppercaseFirstChar()

        val cmakeOutputDir = project.cmakeOutputDir
        val cmakeConfigOutputDir = cmakeOutputDir.map { it.dir("config") }
        val wrapFile = getWrapFile(javaWrapper, project)

        val cmakeConfigTaskName = "generateCmakeConfig${nameCapitalized}"
        val cmakeConfigTask = when {
            project.tasks.none { it.name == cmakeConfigTaskName } -> {
                project.tasks.register(cmakeConfigTaskName, GenerateCmakeConfigTask::class) {
                    this.cppFile.set(wrapFile)
                    this.outputDir.set(cmakeConfigOutputDir)
                    this.libName.set(name)

                    this.group = GROUP
                }
            }
            else -> project.tasks.named(cmakeConfigTaskName, GenerateCmakeConfigTask::class)
        }

        return cmakeConfigTask
    }

    private fun createOrFindCmakePreloadScriptTask(
        project: Project,
        cmakeConfigTask: TaskProvider<GenerateCmakeConfigTask>
    ): TaskProvider<GenerateCmakePreloadScriptTask> {

        val cmakeConfigOutputDir = cmakeConfigTask.flatMap { it.outputDir }
        val cmakeOutputDir = project.cmakeOutputDir
        val cmakePreloadOutputDir = cmakeOutputDir.map { it.dir("preload") }

        val cmakePreloadTaskName = "generateCmakePreloadScript"
        val generateCmakePreloadScriptTask = when {
            project.tasks.none { it.name == cmakePreloadTaskName } -> {
                project.tasks.register(cmakePreloadTaskName, GenerateCmakePreloadScriptTask::class) {
                    this.configDir.set(cmakeConfigOutputDir)
                    this.outputDir.set(cmakePreloadOutputDir)
                    this.fileName.set("init_swig.cmake")
                    this.group = GROUP
                }
            }
            else -> project.tasks.named(cmakePreloadTaskName, GenerateCmakePreloadScriptTask::class)
        }

        return generateCmakePreloadScriptTask
    }

    private fun createGenerateJavaSwigWrapperTask(
        project: Project,
        javaWrapper: JavaWrapper,
        variantName: String,
        swigWrapperTask: TaskProvider<Task>,
        cmakeConfigTask: TaskProvider<GenerateCmakeConfigTask>
    ): TaskProvider<GenerateSwigWrapperTask> {
        val packageName = javaWrapper.packageName
        val interfaceFile = javaWrapper.interfaceFile
        val sourceFolders = javaWrapper.sourceFolders.files

        val swigDir = project.swigDir
        val javaOutputDir = swigDir.map { it.dir("java/${variantName}") }

        val wrapFile = getWrapFile(javaWrapper, project)

        val nameCapitalized = javaWrapper.name.uppercaseFirstChar()
        val variantNameCapitalized = variantName.uppercaseFirstChar()
        val taskName = "generate${nameCapitalized}${variantNameCapitalized}JavaSwigWrapper"
        val generateSwigWrapperTask = project.tasks.register(taskName, GenerateSwigWrapperTask::class) {
            this.packageName.set(packageName)
            this.interfaceFile.set(interfaceFile)
            this.source(interfaceFile)
            this.outputDir.set(javaOutputDir)

            this.sourceDirs.set(sourceFolders)
            this.wrapFile.set(wrapFile)

            this.rootPath = project.rootDir.path

            this.group = GROUP
        }

        swigWrapperTask.dependsOn(generateSwigWrapperTask)
        cmakeConfigTask.dependsOn(generateSwigWrapperTask)

        return generateSwigWrapperTask
    }

    private val Project.swigDir: Provider<Directory>
        get() = this.layout.buildDirectory.dir("generated/swig")

    private val Project.cmakeOutputDir: Provider<Directory>
        get() = this.swigDir.map { it.dir("cmake") }

    private fun getWrapFile(javaWrapper: JavaWrapper, project: Project): Provider<RegularFile> {
        val interfaceFile = javaWrapper.interfaceFile
        val cppOutputDir = project.swigDir.map { it.dir("cpp") }
        val interfaceWrapFileName = "${interfaceFile.nameWithoutExtension}_wrap.cpp"
        return cppOutputDir.map { it.file(interfaceWrapFileName) }
    }
}