package com.altlifegames.altlife.model

data class Property(
    val name: String,
    val type: String, // "apartment", "house", "mansion"
    val location: String,
    val purchasePrice: Int,
    val monthlyCost: Int,
    val value: Int,
    val isOwned: Boolean = false
)

class HousingSystem {
    companion object {
        val PROPERTIES = listOf(
            Property("Studio Apartment", "apartment", "City", 80000, 800, 80000),
            Property("2-Bedroom House", "house", "Suburbs", 250000, 1500, 250000),
            Property("Luxury Mansion", "mansion", "Metropolis", 2000000, 5000, 2000000)
        )
        
        fun getAffordableProperties(player: Player, location: String): List<Property> {
            return PROPERTIES.filter { property ->
                property.location == location && player.canAfford(property.purchasePrice)
            }
        }
        
        fun calculatePropertyValueChange(properties: MutableList<Property>) {
            for (i in properties.indices) {
                val property = properties[i]
                // Property value fluctuation based on location economy
                val changePercent = (Math.random() * 10) - 5 // -5% to +5%
                val changeAmount = (property.value * changePercent / 100).toInt()
                val newValue = property.value + changeAmount
                properties[i] = property.copy(value = newValue)
            }
        }
    }
}