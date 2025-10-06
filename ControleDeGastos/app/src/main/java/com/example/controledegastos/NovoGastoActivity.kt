package com.example.controledegastos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class NovoGastoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUTO = "EXTRA_PRODUTO"
        const val EXTRA_VALOR = "EXTRA_VALOR"
        const val EXTRA_CATEGORIA = "EXTRA_CATEGORIA"
        const val EXTRA_POSITION = "EXTRA_POSITION"
    }

    private var itemPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_gasto)

        val categoriaSpinner: Spinner = findViewById(R.id.categoriaSpinner)
        val salvarButton: Button = findViewById(R.id.salvarButton)
        val produtoEditText: EditText = findViewById(R.id.produtoEditText)
        val valorEditText: EditText = findViewById(R.id.valorEditText)

        val categorias = listOf("Alimentação", "Transporte", "Moradia", "Lazer", "Outros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriaSpinner.adapter = adapter

        if (intent.hasExtra(EXTRA_PRODUTO)) {
            produtoEditText.setText(intent.getStringExtra(EXTRA_PRODUTO))
            // Recebemos o Double e convertemos para String para exibir no campo de texto
            valorEditText.setText(intent.getDoubleExtra(EXTRA_VALOR, 0.0).toString())

            val categoriaRecebida = intent.getStringExtra(EXTRA_CATEGORIA)
            val categoriaPosition = categorias.indexOf(categoriaRecebida)
            if (categoriaPosition >= 0) {
                categoriaSpinner.setSelection(categoriaPosition)
            }
            itemPosition = intent.getIntExtra(EXTRA_POSITION, -1)
        }

        salvarButton.setOnClickListener {
            val produto = produtoEditText.text.toString()
            val valorStr = valorEditText.text.toString()
            val categoria = categoriaSpinner.selectedItem.toString()

            // Validação para garantir que o valor é um número válido
            val valor = valorStr.toDoubleOrNull()

            if (produto.isNotEmpty() && valor != null) {
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_PRODUTO, produto)
                resultIntent.putExtra(EXTRA_VALOR, valor) // Enviando o Double
                resultIntent.putExtra(EXTRA_CATEGORIA, categoria)
                resultIntent.putExtra(EXTRA_POSITION, itemPosition)

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos com valores válidos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}