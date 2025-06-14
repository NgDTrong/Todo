package com.example.demotodo.adapter

import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImagePagerAdapter(
    private val imageBytes: List<ByteArray>
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView= ImageView(parent.context)
        imageView.layoutParams=ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType=ImageView.ScaleType.CENTER_CROP
        return  ImageViewHolder(imageView)
    }

    override fun getItemCount(): Int= imageBytes.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val bitmap= BitmapFactory.decodeByteArray(imageBytes[position],0,imageBytes[position].size)
        holder.imageView.setImageBitmap(bitmap)
    }
}