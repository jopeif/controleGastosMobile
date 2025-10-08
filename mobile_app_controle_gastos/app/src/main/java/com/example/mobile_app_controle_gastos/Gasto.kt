package com.example.mobile_app_controle_gastos


data class Gasto(val titulo: String, val valor: Double) {


    override fun toString(): String {
        return "$titulo - R$ ${String.format("%.2f", valor)}"
    }
}