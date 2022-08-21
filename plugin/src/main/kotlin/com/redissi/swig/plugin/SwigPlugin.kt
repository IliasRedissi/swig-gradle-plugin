/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.redissi.swig.plugin

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SwigPlugin: Plugin<Project> {
    public companion object {
        internal const val GROUP = "swig"
    }

    private lateinit var extension: SwigExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("swig", SwigExtension::class.java)
        extension.project = project

        project.tasks.register("generateSwigWrapper") {
            group = GROUP
        }

        val android = project.extensions.getByType(CommonExtension::class.java)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant: Variant ->
            extension.targetLanguages.forEach { it.configureTask(android, variant) }
        }
    }
}
