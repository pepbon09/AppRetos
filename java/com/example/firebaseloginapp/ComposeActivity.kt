package com.example.firebaseloginapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebaseloginapp.ui.theme.FirebaseLoginAppTheme

class ComposeActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseLoginAppTheme {
                MyApp {
                    startActivity(ProfileActivity.newIntent(this,it))
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun MyApp(navigateToProfile: (Perro) -> Unit) {
    Scaffold(
        content = {BarkHomeContent(navigateToProfile = navigateToProfile)}
    )
}

@ExperimentalFoundationApi
@Composable
fun BarkHomeContent(navigateToProfile: (Perro) -> Unit) {
    val perros = remember {DataProvider.perrosList}
    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = perros,
            itemContent = {
                PerrosListItem(perro = it, navigateToProfile)
            })
    }
}