package com.example.mobile_app_controle_gastos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var totalGeralText: TextView
    private lateinit var listViewGastos: ListView
    private val listaGastos = mutableListOf<Gasto>()
    private lateinit var adapter: ArrayAdapter<Gasto>


    companion object {
        private const val ADD_REQUEST_CODE = 1
        private const val EDIT_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = ""
        supportActionBar?.setBackgroundDrawable(null)



        totalGeralText = findViewById(R.id.totalGeral)
        listViewGastos = findViewById(R.id.listViewGastos)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaGastos)
        listViewGastos.adapter = adapter


        listViewGastos.setOnItemClickListener { parent, view, position, id ->


            mostrarDialogoOpcoes(position)
        }

        atualizarTotal()
    }


    private fun mostrarDialogoOpcoes(position: Int) {
        val gastoSelecionado = listaGastos[position]
        val options = arrayOf("Editar", "Excluir")

        AlertDialog.Builder(this)
            .setTitle("O que deseja fazer?")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {

                        val intent = Intent(this, NovoGastoActivity::class.java)
                        intent.putExtra("titulo", gastoSelecionado.titulo)
                        intent.putExtra("valor", gastoSelecionado.valor)
                        intent.putExtra("position", position)
                        startActivityForResult(intent, EDIT_REQUEST_CODE)
                    }
                    1 -> {

                        listaGastos.removeAt(position)
                        adapter.notifyDataSetChanged()
                        atualizarTotal()
                        Toast.makeText(this, "Gasto excluído", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.novoGasto -> {
                val intent = Intent(this, NovoGastoActivity::class.java)
                startActivityForResult(intent, ADD_REQUEST_CODE)
                true
            }

            R.id.limpar -> {
                listaGastos.clear()
                adapter.notifyDataSetChanged()
                atualizarTotal()
                true
            }
            R.id.sobre -> {
                Toast.makeText(
                    this,
                    "Versão 1.0\nApp de controle de gastos",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val titulo = data.getStringExtra("titulo") ?: "Sem título"
            val valor = data.getDoubleExtra("valor", 0.0)

            when (requestCode) {

                ADD_REQUEST_CODE -> {
                    val novoGasto = Gasto(titulo, valor)
                    listaGastos.add(novoGasto)
                }

                EDIT_REQUEST_CODE -> {
                    val position = data.getIntExtra("position", -1)
                    if (position != -1) {
                        val gastoEditado = Gasto(titulo, valor)
                        listaGastos[position] = gastoEditado
                    }
                }
            }
            adapter.notifyDataSetChanged()
            atualizarTotal()
        }
    }

    private fun atualizarTotal() {
        val total = listaGastos.sumOf { it.valor }
        totalGeralText.text = "Total Geral: R$ %.2f".format(total)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
}