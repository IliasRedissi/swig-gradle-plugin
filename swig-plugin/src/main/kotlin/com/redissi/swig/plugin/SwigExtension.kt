package com.redissi.swig.plugin

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public open class SwigExtension @Inject constructor(private val objects: ObjectFactory) {

    internal lateinit var project: Project

    internal val targetLanguages: MutableList<TargetLanguageWrapper> = mutableListOf()

    public fun java(name: String, config: JavaLanguageWrapper.() -> Unit) {
        val java = JavaLanguageWrapper(project, name).apply(config)
        check(targetLanguages.all { it.name != name }) {  "There is already a wrapper defined for $name" }
        targetLanguages.add(java)
    }

}

