package com.example.services.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class View : ViewModel() {

    fun service(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val intent = Intent(context, Provider::class.java)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    fun workManager(context: Context, request: OneTimeWorkRequest) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                WorkManager.getInstance(context).enqueueUniqueWork("import",
                    ExistingWorkPolicy.REPLACE, request)
            }
        }
    }
    private val catFactsMut = MutableLiveData<List<Fact>>()
    val catFacts: LiveData<List<Fact>> = catFactsMut

    fun updateCatFacts(catFacts: List<Fact>) {
        catFactsMut.postValue(catFacts)
    }
}