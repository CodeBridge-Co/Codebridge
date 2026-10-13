package za.co.eduvos.codebridge.data

data class Challenge(
    val id: Int,
    val title: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val category: String,   // "Array", "String", "Hash Table", "Dynamic Programming", "Trees"
    val acceptanceRate: String,
    val isCompleted: Boolean = false,
    val inProgress: Boolean = false,
    val progressPercent: Int = 0
)
