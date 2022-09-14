package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class AccountBalanceData(
    @SerializedName("0") val cashBalance: Balance,
    @SerializedName("3") val coinBalance: Balance,
    @SerializedName("4") val diamondBalance: Balance,
)

data class Balance(
    val balance: Float
)