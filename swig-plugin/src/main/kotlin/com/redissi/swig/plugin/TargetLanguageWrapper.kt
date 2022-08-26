package com.redissi.swig.plugin

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

public abstract class TargetLanguageWrapper(
    internal val project: Project,
    internal val targetLanguage: String,
    public val name: String
) {

    public var interfaceFile: File? = null

    public var sourceFolders: FileCollection? = null

    internal abstract fun configureTask(androidExtension: CommonExtension<*, *, *, *>, variant: Variant)
}