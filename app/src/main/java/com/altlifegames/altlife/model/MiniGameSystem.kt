package com.altlifegames.altlife.model

data class MiniGame(
    val id: String,
    val name: String,
    val type: String, // "puzzle", "skill", "chance"
    val difficulty: Int, // 1-10
    val skillRequired: String? = null,
    val skillLevelRequired: Int = 0,
    val description: String
)

class MiniGameSystem {
    companion object {
        val MINI_GAMES = listOf(
            MiniGame("coding_challenge", "Coding Challenge", "skill", 7, "Programming", 5,
                    "Solve programming problems to improve your skills"),
            MiniGame("art_contest", "Art Contest", "skill", 5, "Art", 3,
                    "Enter an art competition to showcase your talent"),
            MiniGame("sports_match", "Sports Match", "chance", 6, null, 0,
                    "Play in a local sports tournament"),
            MiniGame("music_performance", "Music Performance", "skill", 8, "Music", 6,
                    "Perform at a local venue")
        )
        
        fun playMiniGame(game: MiniGame, player: Player, skills: Map<String, Skill>): MiniGameResult {
            val baseSuccessChance = 0.5
            var successChance = baseSuccessChance
            
            // Modify chance based on skills
            if (game.skillRequired != null) {
                val skillLevel = skills[game.skillRequired]?.level ?: 0
                successChance += (skillLevel * 0.05) // +5% per skill level
                successChance -= (game.difficulty * 0.03) // -3% per difficulty level
            }
            
            // Add some randomness
            successChance += (Math.random() * 0.2) - 0.1 // ±10%
            
            val isSuccess = Math.random() < successChance.coerceIn(0.1, 0.95)
            
            return if (isSuccess) {
                MiniGameResult(
                    true,
                    "Great success! You performed excellently.",
                    mapOf(
                        "money" to (100 * game.difficulty),
                        "happiness" to (5 * game.difficulty),
                        "social" to (3 * game.difficulty)
                    ),
                    game.skillRequired?.let { skill -> mapOf(skill to 10) } ?: emptyMap()
                )
            } else {
                MiniGameResult(
                    false,
                    "It didn't go as well as you hoped, but you learned something.",
                    mapOf(
                        "happiness" to -5,
                        "money" to -(20 * game.difficulty)
                    ),
                    game.skillRequired?.let { skill -> mapOf(skill to 2) } ?: emptyMap()
                )
            }
        }
    }
}

data class MiniGameResult(
    val isSuccess: Boolean,
    val message: String,
    val statChanges: Map<String, Int>,
    val skillExperience: Map<String, Int>
)