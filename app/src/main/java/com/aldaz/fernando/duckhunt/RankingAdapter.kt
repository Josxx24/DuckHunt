package com.aldaz.fernando.duckhunt

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(
    private val dataSet: ArrayList<Player>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    // 🔹 ViewHolder HEADER
    class ViewHolderHeader(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosicion: TextView = view.findViewById(R.id.textViewPosicion)
        val textViewPatosCazados: TextView = view.findViewById(R.id.textViewPatosCazados)
        val textViewUsuario: TextView = view.findViewById(R.id.textViewUsuario)
    }

    // 🔹 ViewHolder NORMAL
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosicion: TextView = view.findViewById(R.id.textViewPosicion)
        val textViewPatosCazados: TextView = view.findViewById(R.id.textViewPatosCazados)
        val textViewUsuario: TextView = view.findViewById(R.id.textViewUsuario)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ranking_list, parent, false)

        return if (viewType == TYPE_HEADER) {
            ViewHolderHeader(view)
        } else {
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ViewHolderHeader) {
            holder.textViewPosicion.text = "#"
            holder.textViewPatosCazados.text = "Patos"
            holder.textViewUsuario.text = "Usuario"

            holder.textViewPosicion.paintFlags =
                holder.textViewPosicion.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.textViewPatosCazados.paintFlags =
                holder.textViewPatosCazados.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.textViewUsuario.paintFlags =
                holder.textViewUsuario.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        if (holder is ViewHolder) {
            val jugador = dataSet[position - 1]

            holder.textViewPosicion.text = position.toString()
            holder.textViewPatosCazados.text = jugador.huntedDucks.toString()
            holder.textViewUsuario.text = jugador.username
        }
    }

    override fun getItemCount(): Int = dataSet.size + 1
}
