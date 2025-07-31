package com.fit2081.yuxuan_34286225.nutritrack.data.network.fruit

data class Fruit(
    var name: String,
    var id: Int,
    var family: String,
    var order: String,
    var genus: String,
    var nutritions: Nutrition
)

data class Nutrition(
    var calories: Int,
    var fat: Float,
    var sugar: Float,
    var carbohydrates: Float,
    var protein: Float
)