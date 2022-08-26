package com.redissi.swig.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public open class SwigExtension @Inject constructor(private val objects: ObjectFactory) {

    internal lateinit var project: Project

    public val javaWrapper: NamedDomainObjectContainer<JavaLanguageWrapper> = objects.domainObjectContainer(JavaLanguageWrapper::class.java)

}

