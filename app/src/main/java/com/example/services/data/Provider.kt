package com.example.services.data

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class Provider : Service() {
    private val link = "https://catfact.ninja/facts?limit=6"

    private val cast = Intent("com.example.Services")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cat()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun cat() {
        CoroutineScope(Dispatchers.IO).launch {
            var c: List<Fact> = emptyList()
            try {
                val url = URL(link)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                val response = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                val json = Gson().fromJson(response, Map::class.java)
                c = (json["data"] as List<Map<String, Any>>).map {
                    Fact(
                        fact = it["fact"] as String,
                        length = (it["length"] as Double).toInt()
                    )
                }
                withContext(Dispatchers.Main) {
                    cast.putExtra("catFacts", Gson().toJson(c))
                    sendBroadcast(cast)
                }

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }
}