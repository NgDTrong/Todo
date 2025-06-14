package com.example.demotodo.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.demotodo.InforDetailActivity
import com.example.demotodo.R
import com.example.demotodo.model.Note

class NoteAdapter(
    val context: Context,
    var list: ArrayList<Note>,
    private val deleteListener: OnNoteDeleteListener,

    ) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutNew = view.findViewById<LinearLayout>(R.id.layout_new)
        val tvTile = view.findViewById<TextView>(R.id.tv_title)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val btnDelete = view.findViewById<ImageView>(R.id.btn_delete)
        val cvItem = view.findViewById<CardView>(R.id.cv_item)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteAdapter.ViewHolder, position: Int) {
        val note = list[position]
        holder.tvTile.text = note.tile
        holder.tvContent.text = note.content
        holder.itemView.alpha = 1f
        holder.btnDelete.setOnClickListener {
            deleteListener.onDelete(note)
        }
        holder.cvItem.setOnClickListener {
            goToInfordetail(note, position)
        }
        if (note.isNew) {
            holder.layoutNew.setBackgroundColor(Color.WHITE)
        } else {
            holder.layoutNew.setBackgroundColor(Color.YELLOW)
        }
    }

    private fun goToInfordetail(note: Note, position: Int) {
        val intent = Intent(context, InforDetailActivity::class.java)
        intent.putExtra("note", note)
        intent.putExtra("position", position)
        intent.putExtra("listSize", list.size)
        (context as AppCompatActivity).startActivityForResult(intent, 1)
    }


    interface OnNoteDeleteListener {
        fun onDelete(note: Note) {
        }

    }

    override fun getItemCount(): Int = list.size
    fun updateData(newList: List<Note>) {
        this.list = newList as ArrayList<Note>
        notifyDataSetChanged()
    }
}




