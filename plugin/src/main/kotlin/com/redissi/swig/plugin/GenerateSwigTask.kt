
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

public abstract class GenerateSwigTask @Inject constructor(private val execOperations: ExecOperations) : DefaultTask() {

    init {
        group = "swig"
        description = "Generate SWIG interfaces"
    }

    @get:Input
    public abstract val packageName: Property<String>

    @get:InputFile
    public abstract val interfaceFile: RegularFileProperty

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @TaskAction
    public fun generate() {
        requireNotNull(interfaceFile.get())
        println(outputDir.get().toString())
        println(outputDir.get().toString())
        outputDir.get().asFile.deleteRecursively()
        val outDir = File(outputDir.asFile.get(), packageName.get().replace('.', '/'))
        outDir.mkdirs()
        execOperations.exec {
            commandLine("swig", "-c++", "-java", "-package", packageName.get(), "-outdir", outDir, interfaceFile.asFile.get().absolutePath)
        }
    }

}