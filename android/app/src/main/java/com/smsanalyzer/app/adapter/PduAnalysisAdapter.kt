package com.smsanalyzer.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smsanalyzer.app.R
import com.smsanalyzer.app.model.PduAnalysis
import java.text.SimpleDateFormat
import java.util.*

class PduAnalysisAdapter(
    private val onItemClick: (PduAnalysis) -> Unit
) : RecyclerView.Adapter<PduAnalysisAdapter.ViewHolder>() {

    private var analyses = mutableListOf<PduAnalysis>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderText: TextView = view.findViewById(R.id.text_sender)
        val messageText: TextView = view.findViewById(R.id.text_message)
        val timestampText: TextView = view.findViewById(R.id.text_timestamp)
        val statusText: TextView = view.findViewById(R.id.text_status)
        val pduTypeText: TextView = view.findViewById(R.id.text_pdu_type)
        val encodingText: TextView = view.findViewById(R.id.text_encoding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdu_analysis, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val analysis = analyses[position]
        
        holder.senderText.text = analysis.sender.ifEmpty { "Unknown" }
        holder.messageText.text = analysis.message.ifEmpty { "No message content" }
        holder.timestampText.text = dateFormat.format(analysis.timestamp)
        holder.statusText.text = analysis.status
        holder.pduTypeText.text = analysis.pduType
        holder.encodingText.text = analysis.encoding
        
        // Set status color
        val statusColor = when (analysis.status) {
            "Valid" -> android.graphics.Color.GREEN
            "Invalid", "Error" -> android.graphics.Color.RED
            else -> android.graphics.Color.GRAY
        }
        holder.statusText.setTextColor(statusColor)
        
        holder.itemView.setOnClickListener {
            onItemClick(analysis)
        }
    }

    override fun getItemCount(): Int = analyses.size

    fun updateData(newAnalyses: List<PduAnalysis>) {
        analyses.clear()
        analyses.addAll(newAnalyses)
        notifyDataSetChanged()
    }

    fun addAnalysis(analysis: PduAnalysis) {
        analyses.add(0, analysis)
        notifyItemInserted(0)
    }
}