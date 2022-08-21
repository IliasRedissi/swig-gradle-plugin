package com.redissi.swig.plugin

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.util.*

public class JavaLanguageWrapper(
    project: Project,
    name: String,
    interfaceFile: File? = null,
    @Suppress("MemberVisibilityCanBePrivate")
    public var packageName: String? = null,
    sourceFolders: FileCollection? = null,
) : TargetLanguageWrapper(project, "java", name, sourceFolders, interfaceFile) {

    override fun configureTask(androidExtension: CommonExtension<*, *, *, *>, variant: Variant) {
        val interfaceFile = this.interfaceFile
        requireNotNull(interfaceFile)

        val swigDir = project.layout.buildDirectory.dir("generated/swig")

        val javaOutputDir = swigDir.map { it.dir("$targetLanguage/${variant.name}") }

        val cppOutputDir = swigDir.map { it.dir("cpp") }
        val wrapFile = cppOutputDir.map { it.file("${interfaceFile.nameWithoutExtension}_wrap.cpp") }

        val cmakeOutputDir = swigDir.map { it.dir("cmake") }
        val cmakeConfigOutputDir = cmakeOutputDir.map { it.dir("config") }
        val cmakePreloadOutputDir = cmakeOutputDir.map { it.dir("preload") }

        val nameCapitalized = name.capitalize(Locale.ROOT)
        val variantNameCapitalized = variant.name.capitalize(Locale.ROOT)

        val taskName = "generate${nameCapitalized}${variantNameCapitalized}JavaWrapper"
        val task = project.tasks.register(taskName, GenerateJavaSwigWrapperTask::class.java) {
            this.packageName.set(this@JavaLanguageWrapper.packageName)
            this.interfaceFile.set(this@JavaLanguageWrapper.interfaceFile)
            this.source(this@JavaLanguageWrapper.interfaceFile)
            this.outputDir.set(javaOutputDir)
            this.cppOutputDir.set(cppOutputDir)

            this.sourceDirs.set(this@JavaLanguageWrapper.sourceFolders)
            this.wrapFile.set(wrapFile)

            this.group = SwigPlugin.GROUP
        }

        val cmakeConfigTaskName = "generateCmakeConfig${nameCapitalized}${variantNameCapitalized}"
        val cmakeConfigTask = project.tasks.register(cmakeConfigTaskName, GenerateCmakeConfigTask::class.java) {
            this.cppFile.set(wrapFile)
            this.outputDir.set(cmakeConfigOutputDir)
            this.libName.set(this@JavaLanguageWrapper.name)

            this.group = SwigPlugin.GROUP

            this.dependsOn(task)
        }

        val cmakePreloadTaskName = "generateCmakePreloadScript"
        val generateCmakePreloadScriptTask = if (!project.tasks.any { it.name == cmakePreloadTaskName }) {
             project.tasks.register(
                cmakePreloadTaskName,
                GenerateCmakePreloadScriptTask::class.java
            ) {
                this.configDir.set(cmakeConfigOutputDir)
                this.outputDir.set(cmakePreloadOutputDir)
                this.fileName.set("init_swig.cmake")
                this.group = SwigPlugin.GROUP
            }
        } else {
            project.tasks.named(cmakePreloadTaskName, GenerateCmakePreloadScriptTask::class.java)
        }

        generateCmakePreloadScriptTask.dependsOn(cmakeConfigTaskName)

        variant.externalNativeBuild?.arguments?.add(
            "-C${
                generateCmakePreloadScriptTask.map {
                    File(
                        it.outputDir.get().asFile,
                        it.fileName.get()
                    )
                }.get().absolutePath
            }"
        )

        variant.sources.java.add(task) { it.outputDir }

        project.afterEvaluate {
            project.tasks.withType(ExternalNativeBuildTask::class).forEach {
                if (it.name.contains("buildCMake")) {
                    it.dependsOn(cmakeConfigTask)
                    it.dependsOn(generateCmakePreloadScriptTask)
                }
            }

            project.tasks.named("generateSwigWrapper").configure {
                dependsOn(task)
            }
        }
    }
}
