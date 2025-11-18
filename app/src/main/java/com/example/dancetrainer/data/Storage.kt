fun loadConnections(ctx: Context): List<Connection> {
        val style = currentStyle(ctx) ?: return emptyList()
        val text = readStyleFile(ctx, style, CONNECTIONS_FILE) ?: return emptyList()
        if (text.isBlank()) return emptyList()

        return text.lines()
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split('|')
                if (parts.size < 4) return@mapNotNull null

                val fromId = parts[0]
                val toId = parts[1]
                val works = parts[2] == "1"

                // Clamp into 1..5, default 3
                val rawSmooth = parts[3].toIntOrNull() ?: 3
                val smooth = rawSmooth.coerceIn(1, 5)

                val notes = if (parts.size >= 5) parts[4] else ""
                Connection(
                    fromId = fromId,
                    toId = toId,
                    works = works,
                    smoothness = smooth,
                    notes = notes
                )
            }
    }
