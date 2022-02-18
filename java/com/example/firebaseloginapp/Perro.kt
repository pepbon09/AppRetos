package com.example.firebaseloginapp

import java.io.Serializable

data class Perro(
    val id: Int,
    val title: String,
    val sex: String,
    val age: Int,
    val description: String,
    val puppyImageId: Int = 0
) : Serializable