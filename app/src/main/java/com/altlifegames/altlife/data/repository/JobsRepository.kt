// app/src/main/java/com/altlifegames/altlife/data/repository/JobsRepository.kt
package com.altlifegames.altlife.data.repository

import android.R
import com.altlifegames.domain.model.Job

object JobsRepository {
    val allJobs = listOf(
        // Entry Level Jobs (Level 1)
        Job(
            id = "retail_associate",
            title = "Retail Associate",
            company = "Mall Department Store",
            baseSalary = 18000,
            level = 1,
            educationRequirement = "High School Diploma",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Help customers find products and maintain store displays. Entry-level position with opportunities for advancement."
        ),
        Job(
            id = "fast_food_worker",
            title = "Fast Food Worker",
            company = "Burger Chain",
            baseSalary = 16000,
            level = 1,
            educationRequirement = "None",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Prepare food, take orders, and maintain cleanliness. Flexible hours for students."
        ),
        Job(
            id = "cashier",
            title = "Cashier",
            company = "Grocery Store",
            baseSalary = 17000,
            level = 1,
            educationRequirement = "High School Diploma",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Process customer transactions and provide excellent service."
        ),
        Job(
            id = "janitor",
            title = "Janitor",
            company = "Office Building",
            baseSalary = 20000,
            level = 1,
            educationRequirement = "None",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Maintain cleanliness and safety of facilities during evening hours."
        ),

        // Mid-Level Jobs (Level 2-4)
        Job(
            id = "sales_rep",
            title = "Sales Representative",
            company = "Tech Solutions",
            baseSalary = 32000,
            level = 2,
            educationRequirement = "Bachelor's Degree",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Sell software solutions to businesses. Commission-based income potential.",
            maxSalary = 85000
        ),
        Job(
            id = "marketing_specialist",
            title = "Marketing Specialist",
            company = "Advertising Agency",
            baseSalary = 35000,
            level = 2,
            educationRequirement = "Bachelor's Degree",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Create marketing campaigns and analyze consumer behavior.",
            maxSalary = 95000
        ),
        Job(
            id = "accountant",
            title = "Accountant",
            company = "CPA Firm",
            baseSalary = 38000,
            level = 2,
            educationRequirement = "Bachelor's in Accounting",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Prepare financial records and tax documents for clients.",
            maxSalary = 110000
        ),
        Job(
            id = "software_developer",
            title = "Software Developer",
            company = "Startup Tech",
            baseSalary = 45000,
            level = 3,
            educationRequirement = "Bachelor's in Computer Science",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Design and develop software applications.",
            maxSalary = 150000
        ),
        Job(
            id = "project_manager",
            title = "Project Manager",
            company = "Construction Co",
            baseSalary = 52000,
            level = 3,
            educationRequirement = "Bachelor's Degree",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Oversee construction projects from planning to completion.",
            maxSalary = 130000
        ),
        Job(
            id = "marketing_manager",
            title = "Marketing Manager",
            company = "Global Brand",
            baseSalary = 65000,
            level = 4,
            educationRequirement = "MBA Preferred",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Lead marketing teams and develop brand strategies.",
            maxSalary = 200000
        ),

        // Senior Level Jobs (Level 5-7)
        Job(
            id = "senior_engineer",
            title = "Senior Software Engineer",
            company = "Tech Giant",
            baseSalary = 95000,
            level = 5,
            educationRequirement = "Master's in CS",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Lead engineering teams and architect complex systems.",
            maxSalary = 250000
        ),
        Job(
            id = "finance_director",
            title = "Director of Finance",
            company = "Corporation",
            baseSalary = 120000,
            level = 5,
            educationRequirement = "MBA Required",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Oversee all financial operations and strategic planning.",
            maxSalary = 350000
        ),
        Job(
            id = "architect",
            title = "Senior Architect",
            company = "Design Firm",
            baseSalary = 85000,
            level = 5,
            educationRequirement = "Master's in Architecture",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Design innovative buildings and oversee construction.",
            maxSalary = 220000
        ),
        Job(
            id = "operations_manager",
            title = "Operations Manager",
            company = "Manufacturing",
            baseSalary = 90000,
            level = 6,
            educationRequirement = "MBA Required",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Manage production processes and optimize efficiency.",
            maxSalary = 275000
        ),
        Job(
            id = "research_director",
            title = "Research Director",
            company = "Pharmaceuticals",
            baseSalary = 140000,
            level = 6,
            educationRequirement = "PhD Required",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Lead pharmaceutical research teams developing new medications.",
            maxSalary = 450000
        ),

        // Executive Level Jobs (Level 8-10)
        Job(
            id = "cto",
            title = "Chief Technology Officer",
            company = "Tech Unicorn",
            baseSalary = 250000,
            level = 8,
            educationRequirement = "PhD Preferred",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Set technology vision and drive innovation across the company.",
            maxSalary = 1200000
        ),
        Job(
            id = "cfo",
            title = "Chief Financial Officer",
            company = "Fortune 500",
            baseSalary = 300000,
            level = 9,
            educationRequirement = "MBA Required",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Oversee all financial matters and investor relations.",
            maxSalary = 1500000
        ),
        Job(
            id = "ceo",
            title = "Chief Executive Officer",
            company = "Multinational Corp",
            baseSalary = 500000,
            level = 10,
            educationRequirement = "MBA Required",
            iconRes = R.drawable.ic_menu_gallery,
            description = "Lead the entire organization and set strategic direction.",
            maxSalary = 2500000
        )
    )

    fun getJobsByLevel(level: Int): List<Job> {
        return allJobs.filter { it.level == level }
    }

    fun getJobsByEducation(education: String): List<Job> {
        return allJobs.filter { 
            when(education) {
                "High School" -> it.level <= 2
                "Associate Degree" -> it.level <= 3
                "Bachelor's Degree" -> it.level <= 5
                "Master's Degree" -> it.level <= 7
                "Doctorate" -> it.level <= 10
                else -> true
            }
        }
    }
}