package za.co.eduvos.codebridge.feature.events

import androidx.annotation.ColorRes
data class Event(
    val company: String,
    val title: String,
    val description: String,
    val date: String,
    val venue: String,
    val type: String,          // "Hackathon", "Tech Talk", "Workshop", "Challenge"
    @ColorRes val tileColor: Int
)
