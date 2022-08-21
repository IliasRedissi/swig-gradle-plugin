package com.redissi.swig.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class GenerateCmakePreloadScriptTask : DefaultTask() {
    @get:InputDirectory
    public abstract val configDir: DirectoryProperty

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @get:Internal
    public abstract val fileName: Property<String>

    @TaskAction
    public fun generate() {
        val content = """
            list(APPEND CMAKE_FIND_ROOT_PATH ${configDir.get().asFile.absolutePath})
            set(CMAKE_FIND_ROOT_PATH ${'$'}{CMAKE_FIND_ROOT_PATH} CACHE PATH "" FORCE)
        """.trimIndent()

        val cmakeFile = File(outputDir.asFile.get(), fileName.get())
        cmakeFile.parentFile.mkdirs()
        cmakeFile.writeText(content)
    }

}