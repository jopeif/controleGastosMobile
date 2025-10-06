package com.example.controledegastos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class GastosAdapter(private val items: List<ListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemLongClick(gasto: Gasto)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_GASTO = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTextView: TextView = view.findViewById(R.id.headerTextView)
        val headerTotalTextView: TextView = view.findViewById(R.id.headerTotalTextView) // NOVO
    }

    class GastoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val produtoTextView: TextView = view.findViewById(R.id.produtoTextView)
        val categoriaTextView: TextView = view.findViewById(R.id.categoriaTextView)
        val valorTextView: TextView = view.findViewById(R.id.valorTextView)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.HeaderItem -> VIEW_TYPE_HEADER
            is ListItem.GastoItem -> VIEW_TYPE_GASTO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_GASTO -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gasto, parent, false)
                GastoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = items[position]) {
            is ListItem.HeaderItem -> {
                val headerHolder = (holder as HeaderViewHolder)
                headerHolder.headerTextView.text = currentItem.categoria
                // Formatando o valor Double para moeda
                headerHolder.headerTotalTextView.text = String.format(Locale.getDefault(), "R$ %.2f", currentItem.totalCategoria)
            }
            is ListItem.GastoItem -> {
                val gastoHolder = (holder as GastoViewHolder)
                val gasto = currentItem.gasto

                gastoHolder.produtoTextView.text = gasto.produto
                gastoHolder.categoriaTextView.text = gasto.categoria
                gastoHolder.valorTextView.text = String.format(Locale.getDefault(), "R$ %.2f", gasto.valor)

                gastoHolder.itemView.setOnLongClickListener {
                    listener?.onItemLongClick(gasto)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}