package com.redissi.swig.plugin

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

public abstract class GenerateJavaSwigWrapperTask : GenerateSwigWrapperTask() {

    @get:Input
    public abstract val packageName: Property<String>

    override fun generate() {
        val packageName = packageName.get()
        subFolders = packageName.replace('.', '/')

        addArgs("-java")
        addArgs("-package", packageName)

        super.generate()
    }

}