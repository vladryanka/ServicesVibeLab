package com.example.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.os.Bundle
import com.example.services.data.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.services.ui.theme.ServicesTheme
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.getValue

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.services.data.CatWorker
import com.example.services.data.Fact
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    private val workRequest = OneTimeWorkRequest.Builder(CatWorker::class.java).build()
    private val viewModel: View by viewModels()
    private lateinit var receiver: BroadcastReceiver
    private lateinit var observer: Observer<WorkInfo>
    private lateinit var workDetailInfo: LiveData<WorkInfo>
    private lateinit var navController: NavHostController


    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            var navController = rememberNavController()
            ServicesApp(navController, viewModel, workRequest)
        }
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val catFacts = Gson().fromJson(
                    intent?.getStringExtra("catFacts"),
                    Array<Fact>::class.java
                ).toList()
                viewModel.updateCatFacts(catFacts)
                navController.navigate("s2")
            }
        }

        registerReceiver(receiver, IntentFilter("com.example.services"), Context.RECEIVER_EXPORTED)

        observer = Observer { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val catFacts = Gson().fromJson(
                    workInfo.outputData.getString("catFacts"),
                    Array<Fact>::class.java
                ).toList()
                viewModel.updateCatFacts(catFacts)
                navController.navigate("s2")
            }
        }

        val workManager = WorkManager.getInstance(this)
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(workRequest.id)
        workInfoLiveData.observe(this, observer)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        workDetailInfo.removeObserver(observer)
    }

}

@Composable
fun ServicesApp(navController: NavHostController, viewModel: View, request: OneTimeWorkRequest) {
    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            FirstScreen(viewModel = viewModel, request)
        }
        composable("selectedScreen") {
            SelectedScr(viewModel = viewModel)
        }
    }
}

@Composable
fun SelectedScr(viewModel: View) {

    val catFacts: List<Fact> = emptyList()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.fact),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            itemsIndexed(catFacts) { _,catFact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text(
                        text = catFact.fact,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }


}

@Composable
fun FirstScreen(viewModel: View, request: OneTimeWorkRequest) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "First screen")
        Row {
            Button(onClick = { viewModel.service(context) }) {
                Text("WorkManager", fontSize = 25.sp)
            }
            Button(onClick = { viewModel.workManager(context, request) }) {
                Text("Service", fontSize = 25.sp)
            }
        }
    }
}

@Composable
fun Second(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Second screen")
        Button(onClick = { onClick() }) {
            Text("Go first", fontSize = 25.sp)
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ServicesTheme {
        Greeting("Android")
    }
}