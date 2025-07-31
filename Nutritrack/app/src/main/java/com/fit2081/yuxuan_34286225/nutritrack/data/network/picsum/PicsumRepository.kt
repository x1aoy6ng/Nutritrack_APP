package com.fit2081.yuxuan_34286225.nutritrack.data.network.picsum

class PicsumRepository {
    /**
     * get the url of random image being generated
     */
    fun getRandomImageUrl(): String {
        val randomNumber = (0 .. 1000).random()
        return "https://picsum.photos/700/500?random=$randomNumber"
    }
}