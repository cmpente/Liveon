package com.liveongames.liveon.util

import androidx.annotation.DrawableRes
import com.liveongames.liveon.R
import com.liveongames.liveon.viewmodel.CrimeViewModel

fun getCrimeName(type: CrimeViewModel.CrimeType) = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Pickpocketing"
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Shoplifting"
    CrimeViewModel.CrimeType.VANDALISM -> "Vandalism"
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Petty scam"

    CrimeViewModel.CrimeType.MUGGING -> "Mugging"
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Breaking & entering"
    CrimeViewModel.CrimeType.DRUG_DEALING -> "Drug dealing"
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Counterfeit goods"

    CrimeViewModel.CrimeType.BURGLARY -> "Burglary"
    CrimeViewModel.CrimeType.FRAUD -> "Fraud"
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Arms smuggling"
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Drug trafficking"

    CrimeViewModel.CrimeType.ARMED_ROBBERY -> "Armed robbery"
    CrimeViewModel.CrimeType.EXTORTION -> "Extortion"
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "Kidnapping for ransom"
    CrimeViewModel.CrimeType.PONZI_SCHEME -> "Ponzi scheme"
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Contract killing"
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Dark web sales"
    CrimeViewModel.CrimeType.ART_THEFT -> "Art theft"
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Diamond heist"

    // NEW: Mastermind additions
    CrimeViewModel.CrimeType.BANK_HEIST -> "Bank heist"
    CrimeViewModel.CrimeType.POLITICAL_ASSASSINATION -> "Political assassination"
    CrimeViewModel.CrimeType.CRIME_SYNDICATE -> "Crime syndicate"
}

fun getCrimeDesc(type: CrimeViewModel.CrimeType) = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> "Lift a wallet or phone."
    CrimeViewModel.CrimeType.SHOPLIFTING -> "Swipe small items from a store."
    CrimeViewModel.CrimeType.VANDALISM -> "Deface property to make a statement."
    CrimeViewModel.CrimeType.PETTY_SCAM -> "Run a street con."

    CrimeViewModel.CrimeType.MUGGING -> "Corner a mark and demand valuables."
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> "Slip into a building."
    CrimeViewModel.CrimeType.DRUG_DEALING -> "Move small product to buyers."
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> "Sell convincing fakes."

    CrimeViewModel.CrimeType.BURGLARY -> "Hit a residence or business."
    CrimeViewModel.CrimeType.FRAUD -> "Confidence games at scale."
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> "Move weapons quietly."
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> "Transport heavy product."

    CrimeViewModel.CrimeType.ARMED_ROBBERY -> "High-stakes, high-risk robbery."
    CrimeViewModel.CrimeType.EXTORTION -> "Money by threat or pressure."
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> "Abduct and negotiate."
    CrimeViewModel.CrimeType.PONZI_SCHEME -> "Pay old investors with new."
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> "Assassination for hire."
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> "Illicit marketplace hustle."
    CrimeViewModel.CrimeType.ART_THEFT -> "Steal priceless works."
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> "Rob the vault."

    // NEW: Mastermind additions
    CrimeViewModel.CrimeType.BANK_HEIST -> "Coordinated job against a bank vault."
    CrimeViewModel.CrimeType.POLITICAL_ASSASSINATION -> "Eliminate a high-profile target."
    CrimeViewModel.CrimeType.CRIME_SYNDICATE -> "Orchestrate a long-term criminal network."
}

fun getCrimeDescShort(full: String) = if (full.length <= 36) full else full.take(33) + "…"

@DrawableRes
fun getCrimeIconRes(type: CrimeViewModel.CrimeType): Int = when (type) {
    CrimeViewModel.CrimeType.PICKPOCKETING -> R.drawable.ic_pickpocket
    CrimeViewModel.CrimeType.SHOPLIFTING -> R.drawable.ic_shoplifting
    CrimeViewModel.CrimeType.VANDALISM -> R.drawable.ic_vandalism
    CrimeViewModel.CrimeType.PETTY_SCAM -> R.drawable.ic_petty_scam

    CrimeViewModel.CrimeType.MUGGING -> R.drawable.ic_mugging
    CrimeViewModel.CrimeType.BREAKING_AND_ENTERING -> R.drawable.ic_break_and_enter
    CrimeViewModel.CrimeType.DRUG_DEALING -> R.drawable.ic_drug_deal
    CrimeViewModel.CrimeType.COUNTERFEIT_GOODS -> R.drawable.ic_counterfeit_goods

    CrimeViewModel.CrimeType.BURGLARY -> R.drawable.ic_burglary
    CrimeViewModel.CrimeType.FRAUD -> R.drawable.ic_fraud
    CrimeViewModel.CrimeType.ARMS_SMUGGLING -> R.drawable.ic_arms_smuggling
    CrimeViewModel.CrimeType.DRUG_TRAFFICKING -> R.drawable.ic_drug_trafficking

    CrimeViewModel.CrimeType.ARMED_ROBBERY -> R.drawable.ic_armed_robbery
    CrimeViewModel.CrimeType.EXTORTION -> R.drawable.ic_extortion
    CrimeViewModel.CrimeType.KIDNAPPING_FOR_RANSOM -> R.drawable.ic_kidnapping
    CrimeViewModel.CrimeType.PONZI_SCHEME -> R.drawable.ic_ponzi
    CrimeViewModel.CrimeType.CONTRACT_KILLING -> R.drawable.ic_contract_killing
    CrimeViewModel.CrimeType.DARK_WEB_SALES -> R.drawable.ic_dark_web
    CrimeViewModel.CrimeType.ART_THEFT -> R.drawable.ic_art_theft
    CrimeViewModel.CrimeType.DIAMOND_HEIST -> R.drawable.ic_diamond_heist

    // NEW: temporary icon mappings (swap once you add dedicated art)
    CrimeViewModel.CrimeType.BANK_HEIST -> R.drawable.ic_armed_robbery          // TODO: replace with ic_bank_heist
    CrimeViewModel.CrimeType.POLITICAL_ASSASSINATION -> R.drawable.ic_contract_killing // TODO: replace with ic_political_assassination
    CrimeViewModel.CrimeType.CRIME_SYNDICATE -> R.drawable.ic_extortion         // TODO: replace with ic_crime_syndicate
}

fun rankForNotoriety(n: Int): String = when {
    n < 5 -> "New Face"
    n < 15 -> "Petty Thief"
    n < 30 -> "Street Hustler"
    n < 45 -> "Enforcer"
    n < 60 -> "Fixer"
    n < 75 -> "Shot Caller"
    n < 90 -> "Capo"
    else -> "Kingpin"
}
