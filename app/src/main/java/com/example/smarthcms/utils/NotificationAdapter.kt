package com.example.smarthcms.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarthcms.databinding.NotificationItemBinding
import com.example.smarthcms.models.Notification

class NotificationAdapter(private val context:Context,private var list:ArrayList<Notification>):RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    inner class ViewHolder(private val notificationItemBinding: NotificationItemBinding) :
        RecyclerView.ViewHolder(notificationItemBinding.root) {
        fun bind(notification: Notification) {
            notificationItemBinding.apply {
                tvPatientName.text = notification.username
                tvRoom.text = notification.room
                tvMsg.text = notification.msg
                if (notification.image!= ""){
                    Glide.with(context)
                        .load(notification.image)
                        .into(ivPatient)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(NotificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun filterList(filteredList: ArrayList<Notification>) {
        list = filteredList
        notifyDataSetChanged()
    }
}