package com.altlifegames.altlife.model

data class Personality(
    val traits: Map<String, Int>, // trait_name to level (1-10)
    val preferences: Map<String, Int> // preference to importance (1-10)
)

class PersonalitySystem {
    companion object {
        val PERSONALITY_TRAITS = listOf(
            "Ambitious", "Creative", "Social", "Analytical", "Empathetic",
            "RiskTaker", "Conservative", "Optimistic", "Pessimistic", "Perfectionist"
        )
        
        val LIFE_PREFERENCES = listOf(
            "Career", "Family", "Adventure", "Stability", "Wealth",
            "Knowledge", "Fame", "Health", "Spirituality", "Fun"
        )
        
        fun generatePersonality(): Personality {
            val traits = PERSONALITY_TRAITS.associateWith { 
                (Math.random() * 10 + 1).toInt() 
            }
            
            val preferences = LIFE_PREFERENCES.associateWith { 
                (Math.random() * 10 + 1).toInt() 
            }
            
            return Personality(traits, preferences)
        }
        
        fun applyPersonalityEffects(player: Player, personality: Personality) {
            // Apply trait effects
            val traits = personality.traits
            
            // Ambitious players progress faster in career
            if (traits["Ambitious"] ?: 5 > 7) {
                player.careerLevel = (player.careerLevel * 1.1).toInt()
            }
            
            // Creative players get happiness bonuses from hobbies
            if (traits["Creative"] ?: 5 > 6) {
                player.modifyStats(happinessChange = 2)
            }
            
            // Social players get relationship bonuses
            if (traits["Social"] ?: 5 > 6) {
                player.modifyStats(socialChange = 3)
            }
            
            // Analytical players learn faster
            if (traits["Analytical"] ?: 5 > 7) {
                player.intelligence = (player.intelligence * 1.05).toInt()
            }
        }
    }
}