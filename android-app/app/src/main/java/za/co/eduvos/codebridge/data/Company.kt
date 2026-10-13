package za.co.eduvos.codebridge.data

data class Company(
    val id: Int,
    val name: String,
    val tags: String,           // e.g. "Frontend · Backend · System Design"
    val challengeCount: Int,   // e.g. 12
    val description: String,   // e.g. "Solve real-world problems used by Amazon engineers."
    val challenges: List<Challenge>
)
