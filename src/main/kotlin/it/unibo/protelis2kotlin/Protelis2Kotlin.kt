/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package it.unibo.protelis2kotlin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.RegexOption.MULTILINE
import java.io.File.separator as SEP

/**
 * Protelis file extension.
 */
const val PROTELIS_FILE_EXTENSION = "pt"

/**
 * Data class containing [protelisTypes] information that should be collected during parsing.
 */
private data class Context(var protelisTypes: Set<String> = emptySet()) {
    fun registerProtelisType(type: String) {
        this.protelisTypes += type
    }
}

/**
 * Interface for a "piece of documentation".
 */
private interface DocPiece {
    companion object {

        /**
         * matches a @param tag.
         */
        val docParamRegex = """@param\s+(\w+)\s*([^\n]*)""".toRegex()

        /**
         * matches a @return tag.
         */
        val docReturnRegex = """@return\s+([^\n]*)""".toRegex()

        /**
         * matches other directives.
         */
        val docOtherDirectiveRegex = """@(\w+)\s+([^\n]*)""".toRegex()
    }

    /**
     * Creates a new [DocPiece] that extends this [DocPiece] with some [text].
     */
    fun extendWith(text: String): DocPiece
}

/**
 * Data class for a piece of documentation [text] (like this very comment).
 */
private data class DocText(val text: String) : DocPiece {
    override fun extendWith(text: String): DocPiece {
        return DocText(this.text + text)
    }
}

/**
 * Data class for a piece of documentation describing a function parameter with its [name], [type], and [description].
 */
private data class DocParam(
    val name: String,
    val type: String,
    val description: String
) : DocPiece {
    override fun extendWith(text: String): DocPiece {
        return DocParam(name, type, description + text)
    }
}

/**
 * Data class for a piece of documentation with a [description] a function's [returnType].
 */
private data class DocReturn(
    val returnType: String,
    val description: String
) : DocPiece {
    override fun extendWith(text: String): DocPiece {
        return DocReturn(returnType, description + text)
    }
}

/**
 * Data class for a generic documentation [directive] `@<directive> [description]`.
 */
private data class DocDirective(
    val directive: String,
    val description: String
) : DocPiece {
    override fun extendWith(text: String): DocPiece {
        return DocDirective(directive, description + text)
    }
}

/**
 * Data class describing a Protelis function parameter ([name] and [type]).
 */
private data class ProtelisFunArg(val name: String, val type: String)

/**
 * Data class describing a Protelis function: [name], [parameters], [returnType],
 * visibility ([public] or not), and type parameters ([genericTypes]).
 */
private data class ProtelisFun(
    val name: String,
    val parameters: List<ProtelisFunArg> = listOf(),
    val returnType: String = "",
    val public: Boolean = false,
    val genericTypes: Set<String> = setOf()
)

/**
 * Data class containing the various [documentationPieces] for a Protelis function.
 */
private data class ProtelisFunDoc(val documentationPieces: List<DocPiece>)

/**
 * Data class pairing a Protelis [function] with its [docs].
 */
private data class ProtelisItem(val function: ProtelisFun, val docs: ProtelisFunDoc)

/**
 * Parses a type and returns both the parsed type and the remaining text.
 * @param line The text line to be parsed
 */
private fun parseTypeAndRest(line: String): Pair<String, String> {
    // Works by finding the first comma which is not contained within parentheses
    var stillType = true
    var k = 0
    var parentheses = ""
    val type = line.takeWhile { c ->
        k++
        val cond = (c != ',' || stillType) && !(c == ',' && k > 0 && parentheses.isEmpty())
        if (stillType && (c == '(' || c == '[')) parentheses += c
        if (stillType && (c == ')' || c == ']')) {
            parentheses = parentheses.dropLast(1)
            if (parentheses.isEmpty()) stillType = false
        }
        cond
    }
    return Pair(type, line.substring(k).trim())
}

/**
 * Parses the documentation of a Protelis function.
 * @param doc The documentation string to be parsed
 * @return [ProtelisFunDoc]
 */
private fun parseDoc(doc: String): ProtelisFunDoc {
    var txt = ""
    val pieces: MutableList<DocPiece> = mutableListOf()
    doc.lines().map { """\s*\*\s*""".trimMargin().toRegex().replace(it, "").trim() }.forEach { partialtxt ->
        if (!partialtxt.startsWith("@")) {
            if (pieces.isEmpty()) txt += if (txt.isEmpty()) partialtxt else "\n $partialtxt"
            else {
                val last = pieces.last()
                pieces.remove(last)
                pieces.add(last.extendWith(" $partialtxt"))
            }
        } else {
            DocPiece.docParamRegex.matchEntire(partialtxt)?.let { matchRes ->
                val gs = matchRes.groupValues
                val (type, desc) = parseTypeAndRest(gs[2])
                pieces.add(DocParam(gs[1], type, desc))
                return@forEach
            }

            DocPiece.docReturnRegex.matchEntire(partialtxt)?.let { matchRes ->
                val gs = matchRes.groupValues
                val (type, desc) = parseTypeAndRest(gs[1])
                pieces.add(DocReturn(type, desc))
                return@forEach
            }

            DocPiece.docOtherDirectiveRegex.matchEntire(partialtxt)?.let { matchRes ->
                val directive = matchRes.groupValues[1]
                val desc = matchRes.groupValues[2]
                pieces.add(DocDirective(directive, desc))
                return@forEach
            }
        }
    }
    if (txt.isNotEmpty()) {
        pieces.add(0, DocText(txt))
    }
    return ProtelisFunDoc(pieces)
}

/**
 * Parses a Protelis function definition.
 * @param fline The string of a Protelis function definition to be parsed
 * @return [ProtelisFun]
 */
private fun parseProtelisFunction(fline: String): ProtelisFun {
    return ProtelisFun(
        name = checkNotNull("""def\s+(\w+)\s*\(""".toRegex().find(fline)?.groupValues?.get(1)) {
            "Cannot parse function name in: $fline"
        },
        parameters =
        """\(([^\)]*)\)""".toRegex().find(fline)?.groupValues?.get(1)?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.map {
                // if (!"""\w""".toRegex().matches(it)) throw IllegalStateException("Bad argument name: $it")
                ProtelisFunArg(it.trim(), "")
            }
            ?.toList()
            ?: error("Cannot parse arglist in: $fline"),
        public = "(public\\s+def)".toRegex().find(fline) != null
    )
}

/**
 * Parses Protelis source code into a list of [ProtelisItem]s.
 * @param content The string of Protelis source code to be parsed
 */
private fun parseFile(content: String): List<ProtelisItem> {
    val pitems = mutableListOf<ProtelisItem>()

    """^\s*(/\*\*(.*?)\*/)?\n*((^|[\w\s]*\s)def\s[^\{]*?\{)"""
        .toRegex(setOf(MULTILINE, DOT_MATCHES_ALL))
        .findAll(content)
        .forEach { matchRes ->
            val groups = matchRes.groupValues
            val doc = groups[2]
            val funLine = groups[3]
            val parsedDoc: ProtelisFunDoc = parseDoc(doc)
            // Easy check to control if we actually have a function
            if (!funLine.contains("def")) return@forEach
            val parsedFun = parseProtelisFunction(funLine)
            pitems.add(ProtelisItem(parsedFun, parsedDoc))
        }
    return pitems
}

/**
 * Generates (Dokka ) Kotlin documentation from a [ProtelisFunDoc].
 * @param docs The [ProtelisFunDoc] object encapsulating the docs for a Protelis function
 */
private fun generateKotlinDoc(docs: ProtelisFunDoc): String {
    val docPieces = docs.documentationPieces
    return "/**\n" +
        docPieces.joinToString("\n") { p ->
            when (p) {
                is DocText -> p.text.lines().joinToString("\n") { "  * $it" }
                is DocParam -> "  * @param ${p.name} ${p.description}"
                is DocReturn -> "  * @return ${p.description}"
                is DocDirective -> "  * @${p.directive} ${p.description}"
                else -> ""
            }
        } + "\n  */"
}

/**
 * Generates a Kotlin type from a Protelis type.
 */
private fun generateKotlinType(context: Context, protelisType: String): String = when (protelisType) {
    "" -> "Unit"
    "bool" -> "Boolean"
    "num" -> "Number"
    else ->
        """\(([^\)]*)\)\s*->\s*(.*)""".toRegex().matchEntire(protelisType)?.let { matchRes ->
            val args = matchRes.groupValues[1].split(",").map { generateKotlinType(context, it.trim()) }
            val ret = generateKotlinType(context, matchRes.groupValues[2])
            """(${args.joinToString(",")}) -> $ret"""
        } ?: """\[.*\]""".toRegex().matchEntire(protelisType)?.let { _ ->
            context.registerProtelisType("Tuple")
            "Tuple"
        } ?: if (protelisType.length == 1 && protelisType.any { it.isUpperCase() }) {
            protelisType
        } else if ("""[A-Z]'""".toRegex().matches(protelisType)) {
            "${protelisType[0].inc()}"
        } else if ("""\w+""".toRegex().matches(protelisType)) {
            context.registerProtelisType(protelisType)
            protelisType
        } else {
            "Any"
        }
}

/**
 * Valid Protelis symbols that are not valid in Kotlin (e.g., as they are reserved words) are sanitized.
 */
private fun sanitizeNameForKotlin(name: String): String = when (name) {
    "null" -> "`null`"
    else -> name
}

/**
 * Generates a Kotlin function from a Protelis function descriptor.
 */
private fun generateKotlinFun(context: Context, fn: ProtelisFun): String {
    var genTypesStr = fn.genericTypes.joinToString(",")
    if (genTypesStr.isNotEmpty()) genTypesStr = " <$genTypesStr>"
    return "@Suppress(\"UNUSED_PARAMETER\")\nfun$genTypesStr ${sanitizeNameForKotlin(fn.name)}(" +
        fn.parameters.joinToString(", ") {
            "${sanitizeNameForKotlin(it.name)}: ${
            generateKotlinType(
                context,
                it.type
            )
            }"
        } +
        "): ${generateKotlinType(context, fn.returnType)} = TODO()"
}

/**
 * Generates a Kotlin item (doc + fun signature) from a Protelis item (doc + fun).
 */
private fun generateKotlinItem(context: Context, pitem: ProtelisItem): String {
    val doc = pitem.docs
    val fn = pitem.function
    return generateKotlinDoc(doc) + "\n" + generateKotlinFun(context, fn)
}

/**
 * Generates a string from a list of Protelis items (function and docs pairs).
 */
private fun generateKotlin(context: Context, protelisItems: List<ProtelisItem>): String = protelisItems.map { item ->
    val doc = item.docs
    val fn = item.function
    item.copy(
        function = fn.copy(
            returnType = doc.documentationPieces
                .filterIsInstance<DocReturn>()
                .map { it.returnType }
                .firstOrNull()
                ?: "",
            parameters = fn.parameters
                .map { param ->
                    param.copy(
                        type = doc.documentationPieces
                            .filter { it is DocParam && it.name == param.name }
                            .map { (it as DocParam).type }
                            .firstOrNull()
                            ?: "Any"
                    )
                },
            genericTypes = doc.documentationPieces
                .map { if (it !is DocParam) "" else it.type }
                .flatMap { type ->
                    "([A-Z]'?)".toRegex()
                        .findAll(type)
                        .map {
                            if (it.value.length == 2 && it.value[1] == '\'') {
                                "${it.value[0].inc()}"
                            } else {
                                it.value
                            }
                        }
                        .toList()
                }.toSet()
        )
    )
}.joinToString("\n\n") { generateKotlinItem(context, it) }

/**
 * Turns a Protelis package to a class name using camelcase convention.
 */
private fun packageToClassName(pkg: String): String {
    return pkg.split(':').last().split('_')
        .joinToString("") { it.replaceFirstChar(Char::titlecaseChar) }
}

@Suppress("MagicNumber")
private fun String.sha256(): String =
    BigInteger(MessageDigest.getInstance("SHA-256").digest(toByteArray())).toString(36)

/**
 * Main function: calls [protelis2Kt] without a project.
 *
 * This is to be called with two arguments:
 * 1) The base directory from which recursively looking for Protelis files
 * 2) The destination directory that will contain the output Kotlin files
 */
fun main(args: Array<String>) {
    require(args.size == 2) {
        "USAGE: program <dir> <destDir> <debug>"
    }
    val noProject: Project? = null
    noProject.protelis2Kt(args[0], args[1])
}

/**
 * Reads all Protelis files under a [base] directory, parses them,
 * and generates corresponding Kotlin files in a [destination] directory.
 *
 * This is to be called with two arguments:
 * 1) The base directory from which recursively looking for Protelis files
 * 2) The destination directory that will contain the output Kotlin files
 */
fun Project?.protelis2Kt(base: String, destination: String) {
    val header = "Protelis2Kt"
    val logger = this?.logger ?: object : org.gradle.api.logging.Logger, Logger by LoggerFactory.getLogger(header) {
        override fun isEnabled(level: LogLevel?) = true
        override fun isLifecycleEnabled() = true
        override fun isQuietEnabled(): Boolean = true
        override fun lifecycle(message: String?) = debug(message)
        override fun lifecycle(message: String?, vararg objects: Any?) = debug(message, objects)
        override fun lifecycle(message: String?, throwable: Throwable?) = warn(message, throwable)
        override fun quiet(message: String?) = info(message)
        override fun quiet(message: String?, vararg objects: Any?) = info(message, objects)
        override fun quiet(message: String?, throwable: Throwable?) = warn(message, throwable)
        override fun log(level: LogLevel?, message: String?) = log(level, message, *emptyArray())
        override fun log(level: LogLevel?, message: String?, vararg objects: Any?) = lifecycle(message, objects)
        override fun log(level: LogLevel?, message: String?, throwable: Throwable?) = warn(message, throwable)
    }
    logger.debug("$header base directory: $base\n$header destination directory: $destination")
    var k = 0
    val root = this?.file(base) ?: File(base)
    logger.debug("{}: fetching Protelis files in {}", root.absolutePath)
    root.walkTopDown()
        .filter { it.isFile && it.extension == PROTELIS_FILE_EXTENSION }
        .forEach { file ->
            val fileText: String = file.readText()
            logger.debug("Processing " + file.absolutePath)
            val pkg = "module (.+)".toRegex().find(fileText)?.groupValues?.component2()
                ?: "anonymous_module_${fileText.sha256()}"
            val pkgParts = pkg.split(':')
            val context = Context()
            val protelisItems: List<ProtelisItem> = parseFile(fileText)
            val pkgCode =
                """
                @file:JvmName("${packageToClassName(pkg)}")
                package ${pkgParts.joinToString(".")}
                """.trimIndent()
            val kotlinCode = generateKotlin(context, protelisItems)
            val importCode = context.protelisTypes
                .map {
                    when (it) {
                        "ExecutionContext", "ExecutionEnvironment" -> "org.protelis.vm.$it"
                        "Tuple" -> "org.protelis.lang.datatype.$it"
                        else -> ""
                    }
                }
                .filterNot { it.isEmpty() }
                .joinToString("\n") { "import $it" } + "\n\n"
            val kotlinFullCode = pkgCode + importCode + kotlinCode
            val outPath = "$destination$SEP${pkgParts.joinToString(SEP)}$SEP${file.name.replace(".pt",".kt")}"
            File(outPath).let {
                it.parentFile.mkdirs()
                it.createNewFile()
                it
            }.writeText(kotlinFullCode)
            k++
        }
    logger.lifecycle("$header Converted $k .pt files to Kotlin")
}
