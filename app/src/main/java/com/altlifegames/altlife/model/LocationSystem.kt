package com.altlifegames.altlife.model

data class Location(
    val name: String,
    val description: String,
    val costOfLivingMultiplier: Double,
    val opportunities: List<Opportunity>,
    val canMoveTo: List<String>
)

data class Opportunity(
    val type: String, // "job", "education", "social", "event"
    val name: String,
    val description: String,
    val requirements: Map<String, Int>, // stat_name to min_value
    val rewards: Map<String, Int> // stat_name to change_value
)

class WorldSystem {
    companion object {
        val LOCATIONS = mapOf(
            "SmallTown" to Location(
                "Small Town",
                "A quiet place to start your life",
                0.7,
                listOf(
                    Opportunity("job", "Local Store Clerk", "Work at the general store", 
                              mapOf("social" to 30), mapOf("money" to 1500, "social" to 5)),
                    Opportunity("social", "Community Volunteer", "Help the community", 
                              mapOf("happiness" to 40), mapOf("social" to 15, "happiness" to 10))
                ),
                listOf("City", "CollegeTown")
            ),
            
            "City" to Location(
                "Big City",
                "Where dreams come to life... or die",
                1.5,
                listOf(
                    Opportunity("job", "Corporate Intern", "Start your career", 
                              mapOf("intelligence" to 60, "educationLevel" to 1), 
                              mapOf("money" to 3000, "happiness" to -5)),
                    Opportunity("education", "University", "Pursue higher education", 
                              mapOf("intelligence" to 50), 
                              mapOf("educationLevel" to 1, "intelligence" to 20)),
                    Opportunity("social", "Night Club", "Socialize and party", 
                              mapOf("money" to 200), mapOf("social" to 10, "happiness" to 15))
                ),
                listOf("SmallTown", "CollegeTown", "Metropolis")
            ),
            
            "CollegeTown" to Location(
                "College Town",
                "Academic haven with plenty of learning opportunities",
                1.0,
                listOf(
                    Opportunity("education", "College Education", "4-year degree program", 
                              mapOf("intelligence" to 40), 
                              mapOf("educationLevel" to 2, "intelligence" to 30, "social" to 20))
                ),
                listOf("SmallTown", "City")
            )
        )
    }
}