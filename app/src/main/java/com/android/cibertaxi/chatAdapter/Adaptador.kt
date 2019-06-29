package com.android.cibertaxi.chatAdapter

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.cibertaxi.R


class Adaptador(private var items: ArrayList<Mensaje>, private var context: Context): RecyclerView.Adapter<Adaptador.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var userDto = items[position] // Tomamos los valores insertados en el objeto 'Mensaje'

        // Insertamos los datos en el LinearLayout
        holder?.mensaje?.text = userDto.mensaje // Cambiamos el mensaje


        if(userDto.destino != userDto.idusuario){ // Si el usuario fué el que mando el mensaje
            holder?.mensaje?.background = ContextCompat.getDrawable(context, R.drawable.bg_chat_right) // Le damos el estilo del mensaje a la derecha
            holder?.mensaje?.setTextColor(Color.parseColor("#313131"))
            holder?.ll_mensaje_chat?.gravity = Gravity.RIGHT // Colocamos el mensaje a la derecha
        }else{
            holder?.ll_mensaje_chat?.gravity = Gravity.LEFT
            holder?.mensaje?.background = ContextCompat.getDrawable(context, R.drawable.bg_chat_left) // Le damos el estilo del mensaje a la derecha
            holder?.mensaje?.setTextColor(Color.parseColor("#FFFFFF"))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.mensaje_chat, parent, false) // Inflamos la vista dentro del RecyclerView
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var mensaje: TextView? = null // Inicialiamos el TextView dentro del layout `mensaje_chat`
        var ll_mensaje_chat: LinearLayout // Inicialiamos el LinearLayout dentro del layout `mensaje_chat`

        init {
            this.mensaje = row?.findViewById<TextView>(R.id.tv_mensaje_chat) // Lo asociamos a una varible local
            this.ll_mensaje_chat = row?.findViewById<LinearLayout>(R.id.ll_mensaje_chat) // Lo asociamos a una varible local

        }
    }
}