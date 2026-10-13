package za.co.eduvos.codebridge.data

import java.util.UUID

/**
 * Global User Session Manager.
 * By default, starts in Anonymous Guest Mode with an assigned hash_id.
 * Backends (Java / Python) update this session upon login/registration.
 */
object UserSession {

    val anonymousHashId: String = "usr_hash_" + UUID.randomUUID().toString().substring(0, 8)

    var isLoggedIn: Boolean = false
    var name: String = "Guest Developer"
    var handle: String = "@" + anonymousHashId
    var email: String = ""
    var location: String? = null // Null unless user explicitly shares location

    // Initial stats stand at zero until backend computes activity
    var solvedCount: Int = 0
    var avgScorePercent: Int = 0
    var globalRank: String = "Unranked"

    fun login(registeredName: String, registeredEmail: String, allowLocation: Boolean = false) {
        isLoggedIn = true
        name = registeredName
        email = registeredEmail
        val cleanHandle = registeredName.lowercase().replace(" ", "_")
        handle = "@$cleanHandle"
        if (allowLocation) {
            location = "Johannesburg, South Africa"
        }
    }

    fun logout() {
        isLoggedIn = false
        name = "Guest Developer"
        handle = "@" + anonymousHashId
        email = ""
        location = null
        solvedCount = 0
        avgScorePercent = 0
        globalRank = "Unranked"
    }
}
