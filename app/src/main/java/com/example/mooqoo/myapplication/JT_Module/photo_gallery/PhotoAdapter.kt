package com.example.mooqoo.myapplication.JT_Module.photo_gallery

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.mooqoo.myapplication.JT_Module.JTGameActivity
import com.example.mooqoo.myapplication.R
import kotlinx.android.synthetic.main.item_photo.view.*

class PhotoAdapter(val photos: List<Int>, val context: Context, val clickListener: JTGameActivity.PhotoClickListener) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false))
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resourceId = photos[position]
        holder.iv_photo.setImageResource(resourceId)
        holder.iv_photo.setOnClickListener { clickListener.onItemClick(holder.itemView, position) }
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val iv_photo: ImageView = view.iv_photo
}