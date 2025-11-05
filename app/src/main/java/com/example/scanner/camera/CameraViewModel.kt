package com.example.scanner.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.example.scanner.Feature
import com.example.scanner.GoogleVisionAPI
import com.example.scanner.Image
import com.example.scanner.R
import com.example.scanner.RequestItem
import com.example.scanner.VisionRequest
import com.example.scanner.VisionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class CameraViewModel: ViewModel() {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://vision.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GoogleVisionAPI = retrofit.create(GoogleVisionAPI::class.java)

    fun getEncodedStringFromBitmap(bitmap: Bitmap) : String{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    fun sendImageToAPI(context: Context) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.test_img)
        val imageString = getEncodedStringFromBitmap(bitmap)

        val call = api.detectText(
            "AIzaSyAoSwwDOVrguBX1NqH3N8ebzUkXr_gMamU",
            VisionRequest(requests = listOf(
                RequestItem(
                    image = Image(content = imageString),
                    features = listOf(Feature(type = "TEXT_DETECTION"))
                )
            ))
        )

        call.enqueue(object : Callback<VisionResponse>{
            override fun onResponse(
                call: Call<VisionResponse?>,
                response: Response<VisionResponse?>
            ) {
                if (response.isSuccessful) {
                    println(response.body()?.responses?.first()?.textAnnotations?.first()?.description)
                }
            }

            override fun onFailure(
                call: Call<VisionResponse?>,
                t: Throwable
            ) {
                println("API call failed: ${t.message}")
            }

        })
    }

}