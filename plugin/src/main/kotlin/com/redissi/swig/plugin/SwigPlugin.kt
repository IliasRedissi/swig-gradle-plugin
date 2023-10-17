@file:Suppress("UnstableApiUsage")

package com.redissi.swig.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.ExternalNativeBuildJsonTask
import com.redissi.swig.plugin.extension.JavaWrapper
import com.redissi.swig.plugin.extension.SwigConfig
import com.redissi.swig.plugin.task.GenerateCmakeConfigTask
import com.redissi.swig.plugin.task.GenerateCmakePreloadScriptTask
import com.redissi.swig.plugin.task.GenerateSwigWrapperTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.util.Locale

public class SwigPlugin : Plugin<Project> {
    internal companion object {
        internal const val GROUP = "swig"
        internal const val EXTENSION_NAME = "swig"
        internal const val EXTENSION_CONFIG_NAME = "swigConfig"
        internal const val SWIG_WRAPPER_TASK_NAME = "generateSwigWrapper"
        internal const val SOURCE_NAME = "swig"
    }

    private lateinit var container: NamedDomainObjectContainer<JavaWrapper>

    override fun apply(project: Project) {
        val objects = project.objects

        container = objects.domainObjectContainer(JavaWrapper::class.java) { name ->
            objects.newInstance(JavaWrapper::class, name)
        }

        project.extensions.add(EXTENSION_NAME, container)
        project.extensions.create(EXTENSION_CONFIG_NAME, SwigConfig::class.java)

        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)

        if (androidComponents != null) {
            val swigWrapperTask = project.tasks.register(SWIG_WRAPPER_TASK_NAME) {
                group = GROUP
            }

            val dslExtension = DslExtension.Builder(EXTENSION_NAME)
                .extendProjectWith(container::class.java)
                .build()

            androidComponents.registerExtension(dslExtension) {
                object : VariantExtension {

                }
            }

            androidComponents.registerSourceType(SOURCE_NAME)

            androidComponents.onVariants { variant: Variant ->
                container.forEach { javaWrapper ->
                    configureTasks(project, javaWrapper, variant, swigWrapperTask)
                }
            }
        }
    }

    private fun configureTasks(
        project: Project,
        javaWrapper: JavaWrapper,
        variant: Variant,
        swigWrapperTask: TaskProvider<Task>
    ) {
        val dependenciesWrapper = javaWrapper.dependencies.flatMap { dependencyProject ->
            project.evaluationDependsOn(dependencyProject.path)
            dependencyProject.plugins.getPlugin(SwigPlugin::class.java)
                .container
                .asMap
                .values
        }

        val cmakeConfigTask = createOrFindCmakeConfigTask(project, javaWrapper)
        val cmakePreloadTaskName = createOrFindCmakePreloadScriptTask(project, cmakeConfigTask)

        val externalNativeArguments = variant.externalNativeBuild?.arguments

        val symbols = project.objects.listProperty<String>()

        externalNativeArguments
            ?.map { arguments ->
                arguments.filter { it.startsWith("-D") }.map { it.removePrefix("-D") }
            }
            ?.let { symbols.addAll(it) }

        symbols.addAll(dependenciesWrapper.map { "SWIG_${it.name.uppercase()}_INTERFACE=${it.interfaceFile.asFile.get().absolutePath}" })

        val generateSwigWrapperTask = createGenerateJavaSwigWrapperTask(
            project,
            javaWrapper,
            dependenciesWrapper,
            variant.name,
            symbols,
            swigWrapperTask,
            cmakeConfigTask
        )

        variant.sources.java?.addGeneratedSourceDirectory(generateSwigWrapperTask) { it.outputDir }

        val cmakePreloadFilePath = cmakePreloadTaskName
            .map { File(it.outputDir.get().asFile, it.fileName.get()) }
            .get()
            .absolutePath

        externalNativeArguments?.add("-C${cmakePreloadFilePath}")

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
        dependenciesWrapper: List<JavaWrapper>,
        variantName: String,
        symbols: Provider<List<String>>?,
        swigWrapperTask: TaskProvider<Task>,
        cmakeConfigTask: TaskProvider<GenerateCmakeConfigTask>
    ): TaskProvider<GenerateSwigWrapperTask> {
        val packageName = javaWrapper.packageName
        val interfaceFile = javaWrapper.interfaceFile
        val sourceFolders = mutableSetOf<File>()
        sourceFolders.addAll(javaWrapper.sourceFolders.get())
        dependenciesWrapper.map { it.sourceFolders.get() }.flatten().let { sourceFolders.addAll(it) }
        val extraArguments = javaWrapper.extraArguments.toList()

        requireNotNull(packageName)
        requireNotNull(interfaceFile)

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
            if (symbols != null) {
                this.symbols.set(symbols)
            } else {
                this.symbols.set(emptyList())
            }

            this.extraArguments.set(extraArguments)

            this.sourceDirs.set(sourceFolders)
            this.wrapFile.set(wrapFile)

            this.rootPath.set(project.rootDir.path)

            this.cppProcessing.set(javaWrapper.cppProcessing)

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
        val interfaceFile = javaWrapper.interfaceFile.asFile.get()
        return if (javaWrapper.cppProcessing.get()) {
            val cppOutputDir = project.swigDir.map { it.dir("cpp") }
            val interfaceWrapFileName = "${interfaceFile.nameWithoutExtension}_wrap.cpp"
            cppOutputDir.map { it.file(interfaceWrapFileName) }
        } else {
            val cOutputDir = project.swigDir.map { it.dir("c") }
            val interfaceWrapFileName = "${interfaceFile.nameWithoutExtension}_wrap.c"
            cOutputDir.map { it.file(interfaceWrapFileName) }
        }
    }

    private fun String.uppercaseFirstChar() = this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
