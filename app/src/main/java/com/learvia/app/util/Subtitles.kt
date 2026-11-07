package com.learvia.app.util

/** Parses SRT and WebVTT text into (startMs, endMs, text). Used for user-imported transcripts only. */
object Subtitles {
    private val time = Regex("""(?:(\d+):)?(\d{1,2}):(\d{2})[.,](\d{1,3})""")

    fun parse(raw: String): List<Triple<Long, Long, String>> {
        val text = raw.removePrefix("\uFEFF").replace("\r", "")
        val out = mutableListOf<Triple<Long, Long, String>>()
        for (block in text.split(Regex("\n\\s*\n"))) {
            val lines = block.lines()
            val idx = lines.indexOfFirst { "-->" in it }
            if (idx < 0) continue
            val parts = lines[idx].split("-->")
            val s = ms(parts[0]) ?: continue
            val e = ms(parts[1].trim().substringBefore(' ')) ?: continue
            val t = lines.drop(idx + 1).joinToString(" ").replace(Regex("<[^>]+>"), "").trim()
            if (t.isNotEmpty()) out += Triple(s, e, t)
        }
        return out
    }

    private fun ms(s: String): Long? {
        val m = time.find(s.trim()) ?: return null
        val (h, mi, se, fr) = m.destructured
        val frac = fr.padEnd(3, '0').take(3).toLong()
        return ((h.ifEmpty { "0" }.toLong() * 3600) + mi.toLong() * 60 + se.toLong()) * 1000 + frac
    }
}
