package com.redissi.swig.plugin.extension

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public abstract class SwigExtension @Inject constructor(objects: ObjectFactory) {
    public val javaWrapper: NamedDomainObjectContainer<JavaWrapper> = objects.domainObjectContainer(JavaWrapper::class.java)
}
