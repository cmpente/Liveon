package com.altlifegames.altlife.model

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val objectives: List<QuestObjective>,
    val rewards: QuestRewards,
    val isCompleted: Boolean = false
)

data class QuestObjective(
    val type: String, // "stat", "skill", "event", "time"
    val target: String, // stat name, skill name, etc.
    val requiredValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false
)

data class QuestRewards(
    val money: Int = 0,
    val statChanges: Map<String, Int> = emptyMap(),
    val achievements: List<String> = emptyList()
)

class QuestSystem {
    companion object {
        val QUESTS = listOf(
            Quest(
                "first_job",
                "Get Your First Job",
                "Secure employment to start earning money",
                listOf(
                    QuestObjective("stat", "careerLevel", 1)
                ),
                QuestRewards(1000, mapOf("happiness" to 10), listOf("First Steps"))
            ),
            
            Quest(
                "educated",
                "Pursue Education",
                "Complete higher education to unlock better opportunities",
                listOf(
                    QuestObjective("stat", "educationLevel", 2)
                ),
                QuestRewards(5000, mapOf("intelligence" to 20), listOf("Scholar"))
            ),
            
            Quest(
                "wealthy",
                "Become Wealthy",
                "Accumulate $50,000 in assets",
                listOf(
                    QuestObjective("stat", "money", 50000)
                ),
                QuestRewards(0, mapOf("happiness" to 30), listOf("Wealthy"))
            )
        )
        
        fun checkQuestCompletion(player: Player, quests: MutableList<Quest>) {
            for (i in quests.indices) {
                val quest = quests[i]
                if (!quest.isCompleted) {
                    var allCompleted = true
                    val updatedObjectives = quest.objectives.map { objective ->
                        if (!objective.isCompleted) {
                            val currentValue = when (objective.type) {
                                "stat" -> {
                                    when (objective.target) {
                                        "money" -> player.money
                                        "health" -> player.health
                                        "happiness" -> player.happiness
                                        "intelligence" -> player.intelligence
                                        "educationLevel" -> player.educationLevel
                                        "careerLevel" -> player.careerLevel
                                        else -> 0
                                    }
                                }
                                else -> 0
                            }
                            
                            if (currentValue >= objective.requiredValue) {
                                objective.copy(isCompleted = true)
                            } else {
                                objective
                            }
                        } else {
                            objective
                        }
                    }
                    
                    allCompleted = updatedObjectives.all { it.isCompleted }
                    
                    if (allCompleted) {
                        quests[i] = quest.copy(isCompleted = true)
                        completeQuest(player, quest)
                    } else if (updatedObjectives != quest.objectives) {
                        quests[i] = quest.copy(objectives = updatedObjectives)
                    }
                }
            }
        }
        
        private fun completeQuest(player: Player, quest: Quest) {
            player.addMoney(quest.rewards.money)
            quest.rewards.statChanges.forEach { (stat, value) ->
                when (stat) {
                    "health" -> player.modifyStats(healthChange = value)
                    "happiness" -> player.modifyStats(happinessChange = value)
                    "intelligence" -> player.modifyStats(intelligenceChange = value)
                    "social" -> player.modifyStats(socialChange = value)
                }
            }
            player.achievements.addAll(quest.rewards.achievements)
        }
    }
}