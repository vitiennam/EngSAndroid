package com.example.engsa

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.engsa.ui.theme.EngSATheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream
import java.nio.channels.AsynchronousFileChannel.open

class MainActivity : ComponentActivity() {
    lateinit var engData : Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngSATheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    engData = getJsonDataFromAsset(applicationContext,"EWords446k.json")

//                    val myWebView = WebView(activityContext)
                    mainView(engData)
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
fun getJsonDataFromAsset(context: Context, fileName: String): Array<String> {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return arrayOf("")
    }
    val gson = Gson()
    val engDataType = object : TypeToken<Array<String>>() {}.type

    var engData: Array<String> = gson.fromJson(jsonString, engDataType)

    return engData
}
@Composable
fun mainView(engData : Array<String>)
{
    var state by remember { mutableStateOf(0) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var sampleString = arrayOf("a", "ab", "b", "ba", "bb")
    val titles = listOf("Search", "Flash Card", "History")

    Column {
        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
        if(state == 0) {
            Column() {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search ...") }
                )
                if (searchText != "") {
                    LazyColumn() {
                        items(engData.filter { searchText in it }) { textFilter ->
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = textFilter,
                                style = MaterialTheme.typography.h5
                            )
                        }
                    }
                }


                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = searchText,
                    style = MaterialTheme.typography.h3
                )
            }

        }
        if(state == 1) {
            webView("https://www.apple.com/")
        }
        if(state == 2) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Text tab 3 selected",
                style = MaterialTheme.typography.h3
            )
        }

    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!", modifier = Modifier.padding(24.dp))
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