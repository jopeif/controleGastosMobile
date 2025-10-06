package com.example.controledegastos

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat // NOVO IMPORT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var listaCompletaDeGastos = mutableListOf<Gasto>()
    private lateinit var gastosAdapter: GastosAdapter
    private lateinit var gastosRecyclerView: RecyclerView
    private lateinit var totalGeralTextView: TextView
    private val listaAgrupadaParaExibicao = mutableListOf<ListItem>()
    // Cria um "launcher" (iniciador) para abrir outra Activity e receber um resultado de volta.
 //Substitui o metodo antigo onActivityResult(), que foi descontinuado.
    private val gastoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() // Contrato que indica: "vou iniciar uma Activity e esperar um resultado"
    ) { result -> // Callback chamado quando a Activity que foi aberta termina e retorna um resultado

        // Verifica se o resultado retornado indica sucesso (usuário clicou em "Salvar", por exemplo)
        if (result.resultCode == Activity.RESULT_OK) {

            // Pega o Intent retornado pela outra Activity (pode ser nulo)
            val data: Intent? = result.data

            // Lê os dados enviados pela outra Activity usando as chaves definidas lá (EXTRA_...)
            val produto = data?.getStringExtra(NovoGastoActivity.EXTRA_PRODUTO) // Nome do produto
            val valor = data?.getDoubleExtra(NovoGastoActivity.EXTRA_VALOR, 0.0) ?: 0.0 // Valor do gasto
            val categoria = data?.getStringExtra(NovoGastoActivity.EXTRA_CATEGORIA) // Categoria do gasto
            val position = data?.getIntExtra(NovoGastoActivity.EXTRA_POSITION, -1) ?: -1 // Posição na lista (-1 = novo gasto)

            // Só prossegue se produto e categoria não forem nulos (dados obrigatórios)
            if (produto != null && categoria != null) {

                // Cria um novo objeto Gasto com os dados recebidos
                val gasto = Gasto(produto = produto, valor = valor, categoria = categoria)

                // Se a posição for -1, significa que é um novo gasto; adiciona à lista
                if (position == -1) {
                    listaCompletaDeGastos.add(gasto)
                }
                // Caso contrário, o usuário estava editando um gasto existente; substitui na posição correspondente
                else {
                    listaCompletaDeGastos[position] = gasto
                }

                // Atualiza a lista agrupada (provavelmente atualiza o RecyclerView ou UI relacionada)
                atualizarListaAgrupada()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        WindowCompat.setDecorFitsSystemWindows(window, false)

        atualizarCorIconesStatusBar()


        if (savedInstanceState != null) {
            val listaSalva = savedInstanceState.getParcelableArrayList<Gasto>("lista_gastos")
            if (listaSalva != null) {
                listaCompletaDeGastos = listaSalva
            }
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        gastosRecyclerView = findViewById(R.id.gastosRecyclerView)
        totalGeralTextView = findViewById(R.id.totalGeralTextView)
        gastosAdapter = GastosAdapter(listaAgrupadaParaExibicao)

        gastosRecyclerView.layoutManager = LinearLayoutManager(this)
        gastosRecyclerView.adapter = gastosAdapter

        gastosAdapter.setOnItemClickListener(object : GastosAdapter.OnItemClickListener {
            override fun onItemLongClick(gasto: Gasto) {
                val originalPosition = listaCompletaDeGastos.indexOf(gasto)
                if (originalPosition != -1) {
                    mostrarDialogoOpcoes(originalPosition)
                }
            }
        })

        val novoGastoFab: FloatingActionButton = findViewById(R.id.novoGastoFab)
        novoGastoFab.setOnClickListener {
            val intent = Intent(this, NovoGastoActivity::class.java)
            gastoLauncher.launch(intent)
        }

        atualizarListaAgrupada()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("lista_gastos", ArrayList(listaCompletaDeGastos))
    }

    private fun mostrarDialogoOpcoes(position: Int) {
        val gasto = listaCompletaDeGastos[position]
        AlertDialog.Builder(this)
            .setTitle("O que deseja fazer?")
            .setMessage("Produto: ${gasto.produto}")
            .setPositiveButton("Apagar") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Confirmação")
                    .setMessage("Tem certeza que deseja apagar este item?")
                    .setPositiveButton("Sim") { _, _ ->
                        listaCompletaDeGastos.removeAt(position)
                        atualizarListaAgrupada()
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
            .setNegativeButton("Editar") { _, _ ->
                val intent = Intent(this, NovoGastoActivity::class.java)
                intent.putExtra(NovoGastoActivity.EXTRA_PRODUTO, gasto.produto)
                intent.putExtra(NovoGastoActivity.EXTRA_VALOR, gasto.valor)
                intent.putExtra(NovoGastoActivity.EXTRA_CATEGORIA, gasto.categoria)
                intent.putExtra(NovoGastoActivity.EXTRA_POSITION, position)
                gastoLauncher.launch(intent)
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun atualizarListaAgrupada() {
        val totalGeral = listaCompletaDeGastos.sumOf { it.valor }
        totalGeralTextView.text = String.format(Locale.getDefault(), "Total Geral: R$ %.2f", totalGeral)

        val gastosAgrupados = listaCompletaDeGastos.groupBy { it.categoria }
        listaAgrupadaParaExibicao.clear()

        for ((categoria, gastosDaCategoria) in gastosAgrupados.toSortedMap()) {
            val totalDaCategoria = gastosDaCategoria.sumOf { it.valor }
            listaAgrupadaParaExibicao.add(ListItem.HeaderItem(categoria, totalDaCategoria))
            gastosDaCategoria.forEach { gasto ->
                listaAgrupadaParaExibicao.add(ListItem.GastoItem(gasto))
            }
        }

        gastosAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_theme -> {
                val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                true
            }

            R.id.action_clear_all -> {
                AlertDialog.Builder(this)
                    .setTitle("Confirmar Limpeza")
                    .setMessage("Tem certeza que deseja apagar TODOS os gastos? Esta ação não pode ser desfeita.")
                    .setPositiveButton("Sim, apagar tudo") { _, _ ->
                        listaCompletaDeGastos.clear()
                        atualizarListaAgrupada()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }

            R.id.action_dev_mode -> {
                mostrarMenuDesenvolvedor()
                true
            }

            R.id.action_about -> {
                AlertDialog.Builder(this)
                    .setTitle("Sobre o App")
                    .setMessage("Controle de Compras Mensais\n\nVersão 1.0\n\nDesenvolvido como projeto de faculdade.")
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarMenuDesenvolvedor() {
        val devOptions = arrayOf("Adicionar gastos automaticamente")

        AlertDialog.Builder(this)
            .setTitle("Modo de Desenvolvedor")
            .setItems(devOptions) { _, which ->
                when (which) {
                    0 -> mostrarDialogoAdicionarGastosDev()
                }
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    private fun mostrarDialogoAdicionarGastosDev() {
        val categorias = arrayOf("Alimentação", "Transporte", "Moradia", "Lazer", "Outros")
        var categoriaSelecionada = categorias[0]

        AlertDialog.Builder(this)
            .setTitle("Escolha a categoria")
            .setSingleChoiceItems(categorias, 0) { _, which ->
                categoriaSelecionada = categorias[which]
            }
            .setPositiveButton("Adicionar") { _, _ ->
                for (i in 1..5) {
                    val produto = "Produto de Teste $i"

                    // --- LINHA ALTERADA ---
                    // 1. Gera o double com muitas casas decimais
                    val valorBruto = Random.nextDouble(5.0, 300.0)
                    // 2. Multiplica por 100, arredonda e divide por 100.0 para ter 2 casas decimais
                    val valor = (valorBruto * 100).roundToInt() / 100.0

                    val novoGasto = Gasto(produto, valor, categoriaSelecionada)
                    listaCompletaDeGastos.add(novoGasto)
                }
                atualizarListaAgrupada()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- NOVA FUNÇÃO ADICIONADA ---
    private fun atualizarCorIconesStatusBar() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val isLightTheme = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO
        controller.isAppearanceLightStatusBars = isLightTheme
    }
}