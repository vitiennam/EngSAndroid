package com.example.engsa

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier

import com.example.engsa.ui.theme.EngSATheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment

import androidx.compose.ui.text.AnnotatedString

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState


import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.RealmList

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.channels.AsynchronousFileChannel.open
import kotlinx.coroutines.*

lateinit var engData : Array<String>
lateinit var engData3k : Array<String>
lateinit var searchWord : String
lateinit var randomWord : String
 var userHistory : List<String> = arrayListOf<String>()
lateinit var contextDir : File
lateinit var realm: Realm
lateinit var userDataRealm: UserDataClassRealm
var logingStatus = false
val app = App.create("engs-wnbiw")
lateinit var user : User
val credentials = Credentials.anonymous()
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngSATheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    contextDir = applicationContext.filesDir
                    engData = getJsonDataFromAsset(applicationContext,"EWords446k.json")
                    engData3k = getJsonDataFromAsset(applicationContext,"EWords2.json")
                    userHistory = readJSON( applicationContext, "userHistory.json")




//                    val myWebView = WebView(activityContext)
                    logInApp()

//                    mainView(engData)
//                    Greeting("Android")
                }
            }
        }
    }
}



@Composable
fun webView(webLink : String){

    // Declare a string that contains a url

    // Adding a WebView inside AndroidView
    // with layout as full screen
    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            loadUrl(webLink)
        }
    }, update = {
        it.loadUrl(webLink)
    })
}

@Composable
fun openSearchWord(){
    var state1 by remember { mutableStateOf(0) }
    val titles = listOf("Oxford", "Cambrigde", "GG")

    Column() {
        TabRow(selectedTabIndex = state1) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state1 == index,
                    onClick = { state1 = index },
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
        if(state1 == 0){
            webView(webLink = "https://www.oxfordlearnersdictionaries.com/definition/american_english/${searchWord}?q=${searchWord}")
        }
        if(state1 == 1){
            webView(webLink = "https://dictionary.cambridge.org/dictionary/english-vietnamese/${searchWord}")
        }
        if(state1 == 2){
            webView(webLink = "https://translate.google.com/?hl=vi&sl=en&tl=vi&text=${searchWord}&op=translate")
        }
    }
    
}

suspend fun logIn() {

}
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun logInApp() {

    var clickedButton by remember {
        mutableStateOf(false)
    }
    runBlocking {
        user = app.login(credentials)
        val config = SyncConfiguration.Builder(
            user,
            setOf(UserDataClassRealm::class)
        ) // the SyncConfiguration defaults to Flexible Sync, if a Partition is not specified
            .initialSubscriptions { realm ->
                add(
                    realm.query<UserDataClassRealm>(
                        "ownerId == $0", // owner_id == the logged in user
                        user.id
                    ),
                    "user"
                )

            }
            .build()
        realm = Realm.open(config)
        var countTemp = realm.query<UserDataClassRealm>().find().count()
        if( countTemp == 0) {
//        var temp = app.currentUser?.let { UserDataClassRealm(ownerIdInput = it.id) }
            realm.writeBlocking {
                this.copyToRealm(UserDataClassRealm().apply {
                    ownerId = user.id
                    deviceName = Build.DEVICE.toString()
                    userSearchedWord.addAll(userHistory)

                })
            }
        }

//        if (userDataRealm.userSearchedWord == null) {
//            userDataRealm.userSearchedWord
//        }
//        userDataRealm.userSearchedWord = arrayListOf<String>()
//        userHistory += "tempt"



        userDataRealm = realm.query<UserDataClassRealm>().first().find()!!
        if (userDataRealm != null)
        {
            logingStatus = true
        }

    }
    appNav()
    // create a SyncConfiguration
//    if (logingStatus) {
//        appNav()
//    } else {
//        Column() {
//            if(!clickedButton) {
//                Button(onClick = {
//                    clickedButton
////                    logInRealm()
//
//                }, enabled = !clickedButton) {
//                    Text(text = "Click to Log In")
//                }
//            }
//            if(clickedButton) {
//                CircularProgressIndicator()
//            }
//
//        }
//    }
}

fun logInRealm () = io.realm.kotlin.internal.platform.runBlocking {
    var user = app.login(credentials)
    val config = SyncConfiguration.Builder(
        user,
        setOf(UserDataClassRealm::class)
    ) // the SyncConfiguration defaults to Flexible Sync, if a Partition is not specified
        .initialSubscriptions { realm ->
            add(
                realm.query<UserDataClassRealm>(
                    "ownerId == $0", // owner_id == the logged in user
                    user.id
                ),
                "user"
            )

        }
        .build()
    realm = Realm.open(config)
    var userID = user.id.toString()
    var countTemp = realm.query<UserDataClassRealm>().find().count()
    if( countTemp == 0) {
//        var temp = app.currentUser?.let { UserDataClassRealm(ownerIdInput = it.id) }
        realm.writeBlocking {
            this.copyToRealm(UserDataClassRealm().apply {
                ownerId = user.id
                deviceName = Build.DEVICE.toString()
//                userSearchedWord = ArrayList<String>()
            })
        }
    }
    userDataRealm = realm.query<UserDataClassRealm>().first().find()!!

    if (userDataRealm != null)
    {
        logingStatus = true
    }

}



@Composable
fun appNav(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
// Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (currentScreen != null) {
                        Text(currentScreen)
                    } else {
                        Text("NULL")
                    }
                },
                modifier = modifier,
                navigationIcon = {

                    if (navController.previousBackStackEntry != null) {
//                        Log.e(TAG, "THISS ISISS " + navController.previousBackStackEntry.toString())
                        IconButton(onClick = { navController.navigateUp()
                            searchWord = ""
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) {


        NavHost(
            navController = navController,
            startDestination = "mainView",

            ) {
            composable(route = "mainView") {
                mainView(navController = navController)
            }
            composable(route = "webView") {
                openSearchWord()
            }
            composable(route = "appNav") {
                appNav()
            }
        }


    }
}
fun getJsonDataFromAsset(context: Context, fileName: String): Array<String> {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return arrayOf()
    }
    val gson = Gson()
    val engDataType = object : TypeToken<Array<String>>() {}.type

    var engData: Array<String> = gson.fromJson(jsonString, engDataType)

    return engData
}

fun readJSON(context: Context, fileName:String): List<String> {
    val jsonString: String
    try {
        val bufferedReader: BufferedReader = File(context.filesDir, fileName).bufferedReader()
        jsonString = bufferedReader.use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return emptyList()
    }
    val gson = Gson()
    val engDataType = object : TypeToken<List<String>>() {}.type

    var engData: List<String> = gson.fromJson(jsonString, engDataType)

    return engData
}
fun writeJSONtoFile( fileName:String, saveData: List<String>) {

    //Create a Object of Gson
    var gson = Gson()
    //Convert the Json object to JsonString
    var jsonString:String = gson.toJson(saveData)
    //Initialize the File Writer and write into file
    val file= File(contextDir, fileName)
    file.writeText(jsonString)
}
@Composable
fun mainView(navController: NavHostController, modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(0) }

    var searchText by rememberSaveable { mutableStateOf("") }
    val titles = listOf("Search", "Flash Card", "History")


    Column {

        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index
                              if (state == 1) {
                                  randomWord = engData3k.random()
                              }},
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
        if (state == 0) {
            Column() {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search ...") }
                )
                if (searchText != "") {
                    LazyColumn() {
                        items(engData.filter { it.startsWith(searchText) }) { textFilter ->

                            ClickableText(text = AnnotatedString(textFilter), onClick = {
                                searchWord = textFilter
                                userHistory += searchWord
                                runBlocking {
                                    realm.writeBlocking {
                                        userDataRealm = realm.query<UserDataClassRealm>().first().find()!!
                                        userDataRealm = findLatest(userDataRealm)!!
                                        delete(userDataRealm)
                                        this.copyToRealm(UserDataClassRealm().apply {
                                            ownerId =  user.id
                                            deviceName = Build.DEVICE.toString()
                                            userSearchedWord.addAll(userHistory)

                                        })

                                    }
                                }
                                writeJSONtoFile("userHistory.json", userHistory)
                                navController.navigate("webView")

                            }, modifier = Modifier.padding(10.dp))

                        }
                    }



                }
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = searchText,
                    style = MaterialTheme.typography.h3
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = userHistory.count().toString(),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = userDataRealm.userSearchedWord.count().toString(),
                    style = MaterialTheme.typography.h3
                )

            }
        }
        if (state == 1) {
            Column () {

                ClickableText(text = AnnotatedString(randomWord), onClick = {
                    searchWord = randomWord
                    userHistory += searchWord
                    runBlocking {
                        runBlocking {
                            realm.writeBlocking {
                                userDataRealm = realm.query<UserDataClassRealm>().first().find()!!
                                userDataRealm = findLatest(userDataRealm)!!
                                delete(userDataRealm)
                                this.copyToRealm(UserDataClassRealm().apply {
                                    ownerId =  user.id
                                    deviceName = Build.DEVICE.toString()
                                    userSearchedWord.addAll(userHistory)

                                })

                            }
                        }
                    }



                    writeJSONtoFile("userHistory.json", userHistory)
                    navController.navigate("webView")

                })
            }

        }
        if (state == 2) {
            LazyColumn() {
                items(userDataRealm.userSearchedWord) { textFilter ->

                    ClickableText(text = AnnotatedString(textFilter), onClick = {
                        searchWord = textFilter
                        navController.navigate("webView")

                    }, modifier = Modifier.padding(10.dp))

                }
            }
        }

    }
}







//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    lateinit var engData : Array<String>
//    EngSATheme {
//        engData = getJsonDataFromAsset(applicationContext,"EWords446k.json")
//        mainView(engData)
////        Greeting("Android")
//    }
//}