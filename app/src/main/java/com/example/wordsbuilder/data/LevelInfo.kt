data class LevelInfo(
    val words: List<String>,
    val letters: List<Char>,
    val grid: List<PlacedWord>,
    val reward: Int,
    val hintCost: Int
)