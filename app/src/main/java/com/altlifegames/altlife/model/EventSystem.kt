package com.altlifegames.altlife.model

data class GameEvent(
    val title: String,
    val description: String,
    val choices: List<EventChoice>
)

data class EventChoice(
    val text: String,
    val outcome: (Player) -> Unit,
    val condition: (Player) -> Boolean = { true }
)

class EventSystem {
    companion object {
        fun getRandomEvent(player: Player): GameEvent? {
            val events = mutableListOf<GameEvent>()
            
            // Health events
            if (player.health < 30) {
                events.add(GameEvent(
                    "Health Crisis",
                    "You're feeling very weak. What do you do?",
                    listOf(
                        EventChoice("See a doctor (-$500)", { p ->
                            p.addMoney(-500)
                            p.modifyStats(healthChange = 20)
                        }),
                        EventChoice("Ignore it", { p ->
                            p.modifyStats(healthChange = -10)
                        })
                    )
                ))
            }
            
            // Money events
            if (player.money < 500) {
                events.add(GameEvent(
                    "Financial Stress",
                    "Money is tight this month. How do you handle it?",
                    listOf(
                        EventChoice("Take a second job (+$500, -20 happiness)", { p ->
                            p.addMoney(500)
                            p.modifyStats(happinessChange = -20)
                        }),
                        EventChoice("Cut expenses (-$200)", { p ->
                            p.addMoney(-200)
                        })
                    )
                ))
            }
            
            // Positive events based on stats
            if (player.happiness > 80 && player.social > 70) {
                events.add(GameEvent(
                    "Social Success",
                    "Your popularity is growing! You're invited to an exclusive event.",
                    listOf(
                        EventChoice("Attend (+20 social, -$100)", { p ->
                            p.addMoney(-100)
                            p.modifyStats(socialChange = 20)
                        }),
                        EventChoice("Skip it", {})
                    )
                ))
            }
            
            return events.randomOrNull()
        }
    }
}