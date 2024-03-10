package com.example.services.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

class CatWorker(context: Context, parameter: WorkerParameters) : Worker(context, parameter) {

    private val link = "https://catfact.ninja/facts?limit=15"
    private val catFactsLiveData = MutableLiveData<Result>()

    val catFacts: LiveData<Result>
        get() = catFactsLiveData

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun doWork(): Result {
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

            Thread.sleep(3000)

            val outputData = Data.Builder()
                .putString("catFacts", Gson().toJson(c))
                .build()

            catFactsLiveData.postValue(Result.success(outputData))
        } catch (e: Exception) {
            e.printStackTrace()
            catFactsLiveData.postValue(Result.failure())
        }

        return Result.success()
    }
}