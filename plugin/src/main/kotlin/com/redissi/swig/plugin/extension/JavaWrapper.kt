package com.redissi.swig.plugin.extension

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

public abstract class JavaWrapper @Inject constructor(public val name: String) {

    public abstract val packageName: Property<String>

    public abstract val interfaceFile: RegularFileProperty

    public abstract val sourceFolders: ListProperty<File>

    internal val extraArguments: MutableList<String> = mutableListOf()

    public fun extraArguments(vararg arguments: String) {
        extraArguments.addAll(arguments)
    }

    public abstract val cppProcessing: Property<Boolean>

    internal val dependencies = mutableListOf<Project>()

    public fun dependsOn(vararg projects: Project) {
        dependencies.addAll(projects)
    }

    public fun dependsOn(vararg projectDependency: ProjectDependency) {
        dependsOn(*projectDependency.map { it.dependencyProject }.toTypedArray())
    }

    init {
        cppProcessing.convention(true)
    }
}
