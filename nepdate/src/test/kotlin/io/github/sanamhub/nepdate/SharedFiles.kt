package io.github.sanamhub.nepdate

import java.io.File

/** Reads the hub's vectors from `shared/` (the `nepdate.shared` system property, K1-06). */
object SharedFiles {
    private val root: File =
        File(
            System.getProperty("nepdate.shared")
                ?: error("system property nepdate.shared is not set; run the tests through Gradle"),
        )

    fun file(relative: String): File = root.resolve(relative)

    /**
     * Rows of a CSV or TSV file as header-to-cell maps. Splits on [separator] only and never trims
     * a cell: some parse.tsv inputs start or end with a space. Lines starting with `#` are comments.
     */
    fun rows(
        relative: String,
        separator: Char,
    ): List<Map<String, String>> {
        val lines = file(relative).readLines(Charsets.UTF_8).filter { it.isNotEmpty() && !it.startsWith("#") }
        val header = lines.first().split(separator)
        return lines.drop(1).map { line ->
            val cells = line.split(separator)
            check(cells.size == header.size) { "$relative: ${cells.size} cells, header has ${header.size}: $line" }
            header.zip(cells).toMap()
        }
    }

    fun csv(relative: String) = rows(relative, ',')

    fun tsv(relative: String) = rows(relative, '\t')

    /** "2081-04-15" to (2081, 4, 15). */
    fun ymd(text: String): Triple<Int, Int, Int> {
        val parts = text.split('-')
        return Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }
}
