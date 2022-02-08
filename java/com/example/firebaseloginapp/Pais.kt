package com.example.firebaseloginapp

data class Pais(
    val codigo: String,
    val nombre: String,
    val poblacion: Int
)

data class Ciudad(
    val codigo: String,
    val nombre: String,
    val poblacion: Int,
    val cod_pais: String
)
