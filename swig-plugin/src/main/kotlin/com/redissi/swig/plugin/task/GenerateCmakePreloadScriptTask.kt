package com.redissi.swig.plugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class GenerateCmakePreloadScriptTask : DefaultTask() {
    @get:InputDirectory
    abstract val configDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Internal
    abstract val fileName: Property<String>

    @TaskAction
    fun generate() {
        val configPath = configDir.get().asFile.absolutePath.replace('\\', '/')

        val content = """
            list(APPEND CMAKE_FIND_ROOT_PATH "$configPath")
            set(CMAKE_FIND_ROOT_PATH ${'$'}{CMAKE_FIND_ROOT_PATH} CACHE PATH "" FORCE)
        """.trimIndent()

        val cmakeFile = File(outputDir.asFile.get(), fileName.get())
        cmakeFile.parentFile.mkdirs()
        cmakeFile.writeText(content)
    }

}