package com.swings.vitamatrix.utils

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName(Constants.FULL_NAME) val name: String,
    @SerializedName(Constants.HEIGHT) val height: Int,
    @SerializedName(Constants.WEIGHT) val weight: Int,
    @SerializedName(Constants.AGE) val age: Int,
    @SerializedName(Constants.GOAL) val goal: String
)
