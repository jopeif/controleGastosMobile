package com.example.controledegastos

sealed interface ListItem {
    // ADICIONADO totalCategoria
    data class HeaderItem(val categoria: String, val totalCategoria: Double) : ListItem
    data class GastoItem(val gasto: Gasto) : ListItem
}