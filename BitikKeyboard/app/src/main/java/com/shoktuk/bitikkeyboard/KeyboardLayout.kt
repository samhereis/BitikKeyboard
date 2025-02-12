data class KeyboardLayout(
        val name: String,
        val directionality: Int,
        val spaceKey: String,
        val rows: List<List<KeyEntry>>
)