package com.smsanalyzer.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smsanalyzer.app.R
import com.smsanalyzer.app.model.TechnicalDetail

class TechnicalDetailAdapter : RecyclerView.Adapter<TechnicalDetailAdapter.ViewHolder>() {

    private var details = mutableListOf<TechnicalDetail>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fieldText: TextView = view.findViewById(R.id.text_field)
        val valueText: TextView = view.findViewById(R.id.text_value)
        val hexText: TextView = view.findViewById(R.id.text_hex)
        val descriptionText: TextView = view.findViewById(R.id.text_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technical_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detail = details[position]
        
        holder.fieldText.text = detail.field
        holder.valueText.text = detail.value
        holder.hexText.text = detail.hex
        holder.descriptionText.text = detail.description
    }

    override fun getItemCount(): Int = details.size

    fun updateData(newDetails: List<TechnicalDetail>) {
        details.clear()
        details.addAll(newDetails)
        notifyDataSetChanged()
    }
}