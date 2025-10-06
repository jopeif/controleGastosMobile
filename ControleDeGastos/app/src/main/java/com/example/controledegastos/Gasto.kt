package com.example.controledegastos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // 1. ADICIONADO
data class Gasto(
    val produto: String,
    val valor: Double,
    val categoria: String
) : Parcelable