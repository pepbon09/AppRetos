package com.example.firebaseloginapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.firebaseloginapp.ui.theme.FirebaseLoginAppTheme

class ProfileActivity : ComponentActivity() {
    private val perro: Perro by lazy {
        intent?.getSerializableExtra(PERRO_ID) as Perro
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseLoginAppTheme {
                ProfileScreen(perro)
            }
        }
    }

    @Composable
    fun ProfileScreen(perro: Perro) {
        Column(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints {
                Surface {
                    Column {
                        ProfileHeader(
                            perro,
                            this@BoxWithConstraints.maxHeight)
                        ProfileContent(
                            perro
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ProfileHeader(
        perro: Perro,
        containerHeight: Dp
    ) {
        Image(
            modifier = Modifier
                .height(containerHeight - 370.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            painter = painterResource(id = perro.puppyImageId),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }


    @Composable
    private fun ProfileContent(perro: Perro) {
        Column {
            Title(perro)
            ProfileProperty("Genero", perro.sex)
            ProfileProperty("Edad", perro.age.toString())
            ProfileProperty("Descripcion", perro.description)
        }
    }


    @Composable
    private fun Title(
        puppy: Perro
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)) {
            Text(
                text = puppy.title,
                style = MaterialTheme.typography.h3,
                fontWeight = FontWeight.Bold
            )
        }
    }


    @Composable
    fun ProfileProperty(label: String, value: String) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Divider(modifier = Modifier.padding(bottom = 4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.h5,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Visible
            )
        }
    }

    companion object {
        private const val PERRO_ID = "perro_id"
        fun newIntent(context: Context, perro: Perro) =
            Intent(context, ProfileActivity::class.java).apply {
                putExtra(PERRO_ID, perro)
            }
    }
}