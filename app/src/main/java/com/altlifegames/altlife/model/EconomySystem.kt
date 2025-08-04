package com.altlifegames.altlife.model

class EconomySystem {
    companion object {
        fun calculateMonthlyExpenses(player: Player, locationName: String): Int {
            val location = WorldSystem.LOCATIONS[locationName] ?: WorldSystem.LOCATIONS["SmallTown"]!!
            var baseExpenses = 800 // Food, utilities, etc.
            baseExpenses = (baseExpenses * location.costOfLivingMultiplier).toInt()
            return baseExpenses
        }
        
        fun getInvestmentOptions(player: Player): List<String> {
            val options = mutableListOf<String>()
            if (player.age >= 18) {
                options.add("Tech Stock - $1000")
                options.add("Real Estate - $50000")
            }
            if (player.money > 10000) {
                options.add("Startup Business - $25000")
            }
            return options
        }
        
        fun processInvestments(player: Player, investments: MutableList<Pair<String, Int>>) {
            for (i in investments.indices) {
                val (name, value) = investments[i]
                // Random market fluctuation
                val changePercent = (Math.random() * 20) - 10 // -10% to +10%
                val changeAmount = (value * changePercent / 100).toInt()
                val newValue = value + changeAmount
                investments[i] = Pair(name, newValue)
            }
        }
    }
}