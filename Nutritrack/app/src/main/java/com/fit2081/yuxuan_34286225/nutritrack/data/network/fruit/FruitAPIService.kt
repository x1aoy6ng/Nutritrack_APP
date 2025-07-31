package com.fit2081.yuxuan_34286225.nutritrack.data.network.fruit

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface FruitAPIService {
    /**
     * Fetches the fruit information based on the provided name
     * @param name The name of the fruits
     */
    @GET("api/fruit/{name}")
    suspend fun getFruitsByName(@Path("name") name: String): Response<Fruit>

    /**
     * Companion object to provide a factory method for creating an instance of the [FruitAPIService]
     */
    companion object{
        /**
         * The base URL for the FruityVice API
         */
        var BASE_URL = "https://www.fruityvice.com/"

        /**
         * Creates an instance of [FruitAPIService] using Retrofit
         * @return An implementation of [FruitAPIService] for making API calls
         */
        fun create(): FruitAPIService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(FruitAPIService::class.java)
        }
    }
}