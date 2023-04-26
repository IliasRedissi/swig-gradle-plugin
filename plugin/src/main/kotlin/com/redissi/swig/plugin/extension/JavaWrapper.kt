package com.redissi.swig.plugin.extension

import org.gradle.api.file.FileCollection
import java.io.File

public abstract class JavaWrapper(public val name: String) {
    public abstract var packageName: String

    public abstract var interfaceFile: File

    public abstract var sourceFolders: FileCollection
}
