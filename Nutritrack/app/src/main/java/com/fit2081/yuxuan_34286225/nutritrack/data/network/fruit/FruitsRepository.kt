package com.fit2081.yuxuan_34286225.nutritrack.data.network.fruit

class FruitsRepository() {
    // create an instance of the API Service for making network requests
    private val apiService = FruitAPIService.create()

    /**
     * fetches the fruit information based on the fruit name
     */
    suspend fun getFruitsByName(name: String): Fruit? {
        val fruitResponse = apiService.getFruitsByName(name)
        return if (fruitResponse.isSuccessful){
            fruitResponse.body()
        } else {
            null
        }
    }
}