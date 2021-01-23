package com.adillex.mhelper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecycleViewAdapter(private val context: Context) :RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>(){

    private var events : ArrayList<Event>? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var largeTextView: TextView? = null
        init {
            largeTextView = itemView.findViewById(R.id.eventButton)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events?.size?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        events?.get(position)?.let {event->
            holder.largeTextView?.setText(event.nickname)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, EventShowActivity::class.java)
                intent.putExtra("eventId",event.eventId)
                context.startActivity(intent)
            }
        }
    }

    fun setEvents(eventList : ArrayList<Event>){
        events = eventList
        notifyDataSetChanged()
    }
}