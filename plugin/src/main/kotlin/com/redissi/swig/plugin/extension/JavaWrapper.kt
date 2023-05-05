package com.redissi.swig.plugin.extension

import org.gradle.api.file.FileCollection
import java.io.File

public open class JavaWrapper(public val name: String) {
    public var packageName: String? = null

    public var interfaceFile: File? = null

    public var sourceFolders: FileCollection? = null

    public val extraArguments: MutableList<String> = mutableListOf()

    public fun extraArguments(vararg arguments: String) {
        extraArguments.addAll(arguments)
    }
}
