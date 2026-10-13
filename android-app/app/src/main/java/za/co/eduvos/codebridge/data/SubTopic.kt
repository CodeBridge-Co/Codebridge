package za.co.eduvos.codebridge.data

data class SubTopic(
    val id: Int,
    val name: String,           // e.g. "Arrays", "Linked Lists", "Stacks & Queues"
    val solvedCount: Int = 0,   // e.g. 0
    val totalCount: Int = 2,    // e.g. 2 or 8
    val iconType: String = "code" // "code", "tree", "branch", "chart", "cloud"
)
