package com.example.mobile_app_controle_gastos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NovoGastoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_gasto)

        val editTitulo: EditText = findViewById(R.id.editTitulo)
        val editValor: EditText = findViewById(R.id.editValor)
        val btnSalvar: Button = findViewById(R.id.btnSalvar)

        val position = intent.getIntExtra("position", -1)
        if (position != -1) {
            supportActionBar?.title = "Editar Gasto"
            val tituloExistente = intent.getStringExtra("titulo")
            val valorExistente = intent.getDoubleExtra("valor", 0.0)

            editTitulo.setText(tituloExistente)
            editValor.setText(valorExistente.toString())
        } else {
            supportActionBar?.title = "Novo Gasto"
        }

        btnSalvar.setOnClickListener {
            val titulo = editTitulo.text.toString()
            val valor = editValor.text.toString().toDoubleOrNull() ?: 0.0

            if (titulo.isBlank()) {
                editTitulo.error = "Título é obrigatório"
                return@setOnClickListener
            }

            val data = Intent()
            data.putExtra("titulo", titulo)
            data.putExtra("valor", valor)

            if (position != -1) {
                data.putExtra("position", position)
            }

            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}