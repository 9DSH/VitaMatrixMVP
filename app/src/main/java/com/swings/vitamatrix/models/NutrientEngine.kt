package com.example.nutrientengine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --- Data Models ---
data class NutrientCombo(
    val recommended_nutrients: RecommendedNutrients,
    val recommended_foods: List<String>,
    val recommended_supplements: List<Supplement>,
    val recommended_medications: List<Medication>
)

data class RecommendedNutrients(
    val protein: String,
    val carbs: String,
    val fats: String,
    val fiber: String
)

data class Supplement(
    val name: String,
    val dosage: String,
    val purpose: String
)

data class Medication(
    val name: String,
    val dosage: String,
    val purpose: String
)

// --- Engine Class ---
class NutrientEngine(private val context: Context) {

    private var nutrientDatabase: List<NutrientCombo> = emptyList()

    init {
        loadDatabase()
    }

    private fun loadDatabase() {
        try {
            val json = context.assets.open("nutrient_data.json")
                .bufferedReader().use { it.readText() }

            val listType = object : TypeToken<List<NutrientCombo>>() {}.type
            nutrientDatabase = Gson().fromJson(json, listType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllCombos(): List<NutrientCombo> = nutrientDatabase

    fun getCombo(index: Int): NutrientCombo? =
        nutrientDatabase.getOrNull(index)

    fun searchByFood(food: String): List<NutrientCombo> =
        nutrientDatabase.filter { it.recommended_foods.contains(food) }

    fun searchBySupplement(supplement: String): List<NutrientCombo> =
        nutrientDatabase.filter { combo ->
            combo.recommended_supplements.any { it.name.contains(supplement, ignoreCase = true) }
        }

    fun searchByMedication(medication: String): List<NutrientCombo> =
        nutrientDatabase.filter { combo ->
            combo.recommended_medications.any { it.name.contains(medication, ignoreCase = true) }
        }
}
