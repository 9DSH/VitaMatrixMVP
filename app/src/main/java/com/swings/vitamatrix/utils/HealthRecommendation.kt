package com.swings.vitamatrix.utils

import com.google.gson.annotations.SerializedName


data class HealthRecommendation(
    @SerializedName("recommended_nutrients") val nutrients: Nutrients,
    @SerializedName("recommended_foods") val foods: List<String>,
    @SerializedName("recommended_supplements") val supplements: List<HealthItem>,
    @SerializedName("recommended_medications") val medications: List<HealthItem>
)


data class Nutrients(
    @SerializedName("protein") val protein: String,
    @SerializedName("carbs") val carbs: String,
    @SerializedName("fats") val fats: String,
    @SerializedName("fiber") val fiber: String
)


data class HealthItem(
    @SerializedName("name") val name: String,
    @SerializedName("dosage") val dosage: String,
    @SerializedName("purpose") val purpose: String
)