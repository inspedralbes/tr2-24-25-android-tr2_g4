package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.network.Partida

class PartidaAdapter : RecyclerView.Adapter<PartidaAdapter.PartidaViewHolder>() {

    // Lista que contendrá las partidas que se van a mostrar
    private var partidas = listOf<Partida>()

    // Método para actualizar la lista de partidas
    fun setPartidas(partidas: List<Partida>) {
        this.partidas = partidas
        notifyDataSetChanged() // Notifica al adaptador que la lista ha cambiado
    }

    // Crea y devuelve un nuevo ViewHolder para una partida
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidaViewHolder {
        // Inflamos el layout de cada ítem de la lista
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_partida, parent, false)
        return PartidaViewHolder(itemView)
    }

    // Asocia los datos de la partida al ViewHolder
    override fun onBindViewHolder(holder: PartidaViewHolder, position: Int) {
        val partida = partidas[position]
        holder.bind(partida)
    }

    // Devuelve el número de elementos en la lista
    override fun getItemCount(): Int {
        return partidas.size
    }

    // ViewHolder que se encarga de manejar cada item
    inner class PartidaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val codigoTextView: TextView = itemView.findViewById(R.id.tvCodigoPartida)

        // Método para asociar los datos de una partida a los elementos de la vista
        fun bind(partida: Partida) {
            // Enlazamos el código de la partida al TextView
            codigoTextView.text = partida.codigo
        }
    }
}
