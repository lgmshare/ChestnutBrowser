package com.welcome.browser.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.welcome.browser.R
import com.welcome.browser.model.WebNav

class WebNavAdapter : RecyclerView.Adapter<WebNavAdapter.ItemViewHolder>() {

    var dataList: ArrayList<WebNav> = arrayListOf()

    inner class ItemViewHolder : RecyclerView.ViewHolder {

        val serverIcon: ImageView
        val serverName: TextView

        constructor(item: View) : super(item) {
            serverIcon = item.findViewById(R.id.nav_logo)
            serverName = item.findViewById(R.id.nav_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.web_nav_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataList[position]
        holder.serverIcon.setImageResource(item.icon)
        holder.serverName.text = item.name
        holder.itemView.setOnClickListener {
            itemClickCallback?.invoke(item, position)
        }
    }

    var itemClickCallback: ((WebNav, Int) -> Unit)? = null

}