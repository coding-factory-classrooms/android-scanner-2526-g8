package com.example.scanner

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query


interface GoogleVisionAPI  {
    @POST("v1/images:annotate")
    fun detectText(
        @Query("key") apiKey: String,
        @Body body: VisionRequest
    ): Call<VisionResponse>
}