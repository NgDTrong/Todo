package com.example.demotodo.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.demotodo.R
import com.example.demotodo.model.Todo

class TodoAdapter(
    val mContext: Context,
    var list: ArrayList<Todo>,
    val onClickItemTodo: OnClickItemTodo
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val btnDelete = view.findViewById<ImageView>(R.id.btn_delete)
        val cvItem = view.findViewById<CardView>(R.id.cv_item)
        val imageTodo = view.findViewById<LinearLayout>(R.id.img_todo)

    }

    interface OnClickItemTodo {
        fun onDeleteTodo(todo: Todo) {
        }

        fun onUpdateTodo(todo: Todo) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size
    fun updateData(newList: List<Todo>) {
        this.list.clear()
        this.list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = list[position]
        holder.tvTitle.text = todo.title
        holder.tvContent.text = todo.content
        holder.btnDelete.setOnClickListener {
            onClickItemTodo.onDeleteTodo(todo)
        }
        holder.cvItem.setOnClickListener {
            onClickItemTodo.onUpdateTodo(todo)
        }
        val imageData = todo.imageData
        holder.imageTodo.removeAllViews()
        if (!imageData.isNullOrEmpty()) {
            holder.imageTodo.visibility = View.VISIBLE
            imageData.forEach { imageBytes ->
                if (imageBytes.isNotEmpty()) {
                    val imageView = ImageView(mContext).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            mContext.resources.getDimensionPixelSize(R.dimen.img_with),
                            mContext.resources.getDimensionPixelSize(R.dimen.img_height),
                            1f
                        ).apply {
                            setMargins(16, 8, 16, 8)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageBitmap(
                            BitmapFactory.decodeByteArray(
                                imageBytes,
                                0,
                                imageBytes.size
                            )
                        )
                    }
                    holder.imageTodo.addView(imageView)
                }
            }
        } else {
            holder.imageTodo.visibility = View.GONE
        }

//        if (!todo.image.isNullOrEmpty()) {
//            holder.imageTodo.visibility = View.VISIBLE
//            holder.imageTodo.setImageURI(Uri.parse(todo.image))
//        } else {
//            holder.imageTodo.visibility = View.GONE
//        }

    }
}