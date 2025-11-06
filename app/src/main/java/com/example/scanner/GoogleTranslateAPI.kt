package com.example.scanner

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.net.URLEncoder

object TranslateAPI {
    private val client = OkHttpClient()

    fun translate(text: String, targetLang: String, callback: (String?) -> Unit) {
        val normalized = text.replace("\n", " ").replace("\r", "")
        val encoded = URLEncoder.encode(normalized, "UTF-8")
        val url =
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encoded"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(null)

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    val json = JSONArray(body)
                    val translated = json.getJSONArray(0).getJSONArray(0).getString(0)
                    callback(translated)
                } catch (e: Exception) {
                    callback(null)
                }
            }
        })
    }


}
