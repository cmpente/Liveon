package com.altlifegames.altlife.model

data class Skill(
    val name: String,
    val category: String, // "academic", "creative", "physical", "social"
    val level: Int = 0,
    val experience: Int = 0,
    val maxLevel: Int = 10
)

data class Hobby(
    val name: String,
    val skillRequired: String,
    val minSkillLevel: Int,
    val benefits: Map<String, Int> // stat improvements
)

class SkillSystem {
    companion object {
        val SKILLS = listOf(
            "Programming", "Writing", "Art", "Music", "Sports", 
            "Cooking", "Gardening", "Mechanics", "Medicine", "Law"
        )
        
        val HOBBIES = listOf(
            Hobby("Coding", "Programming", 1, mapOf("intelligence" to 2)),
            Hobby("Writing Novels", "Writing", 3, mapOf("intelligence" to 3)),
            Hobby("Painting", "Art", 2, mapOf("happiness" to 5)),
            Hobby("Playing Sports", "Sports", 1, mapOf("health" to 3, "social" to 4)),
            Hobby("Cooking", "Cooking", 1, mapOf("happiness" to 2))
        )
        
        fun getAvailableHobbies(skills: Map<String, Skill>): List<Hobby> {
            return HOBBIES.filter { hobby ->
                skills[hobby.skillRequired]?.level ?: 0 >= hobby.minSkillLevel
            }
        }
        
        fun improveSkill(skillName: String, skills: MutableMap<String, Skill>, amount: Int = 1) {
            val skill = skills.getOrPut(skillName) { Skill(skillName, getSkillCategory(skillName)) }
            if (skill.level < skill.maxLevel) {
                val newExp = skill.experience + amount
                val expForLevel = skill.level * 100 + 100
                
                if (newExp >= expForLevel && skill.level < skill.maxLevel) {
                    skills[skillName] = skill.copy(
                        level = skill.level + 1,
                        experience = newExp - expForLevel
                    )
                } else {
                    skills[skillName] = skill.copy(experience = newExp)
                }
            }
        }
        
        private fun getSkillCategory(skillName: String): String {
            return when (skillName) {
                "Programming", "Math" -> "academic"
                "Writing", "Art", "Music" -> "creative"
                "Sports", "Fitness" -> "physical"
                "Social", "Leadership" -> "social"
                else -> "general"
            }
        }
    }
}