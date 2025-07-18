package com.smsanalyzer.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smsanalyzer.app.adapter.TechnicalDetailAdapter
import com.smsanalyzer.app.model.PduAnalysis
import java.text.SimpleDateFormat
import java.util.*

class PduAnalysisActivity : AppCompatActivity() {

    private lateinit var senderText: TextView
    private lateinit var messageText: TextView
    private lateinit var timestampText: TextView
    private lateinit var statusText: TextView
    private lateinit var pduTypeText: TextView
    private lateinit var encodingText: TextView
    private lateinit var messageLengthText: TextView
    private lateinit var smscLengthText: TextView
    private lateinit var smscTypeText: TextView
    private lateinit var rawPduText: TextView
    private lateinit var errorMessageText: TextView
    private lateinit var technicalDetailsRecycler: RecyclerView
    
    private lateinit var technicalDetailAdapter: TechnicalDetailAdapter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdu_analysis)

        setupViews()
        setupRecyclerView()
        loadPduAnalysis()
    }

    private fun setupViews() {
        senderText = findViewById(R.id.text_sender_detail)
        messageText = findViewById(R.id.text_message_detail)
        timestampText = findViewById(R.id.text_timestamp_detail)
        statusText = findViewById(R.id.text_status_detail)
        pduTypeText = findViewById(R.id.text_pdu_type_detail)
        encodingText = findViewById(R.id.text_encoding_detail)
        messageLengthText = findViewById(R.id.text_message_length_detail)
        smscLengthText = findViewById(R.id.text_smsc_length_detail)
        smscTypeText = findViewById(R.id.text_smsc_type_detail)
        rawPduText = findViewById(R.id.text_raw_pdu_detail)
        errorMessageText = findViewById(R.id.text_error_message_detail)
        technicalDetailsRecycler = findViewById(R.id.recycler_technical_details)
    }

    private fun setupRecyclerView() {
        technicalDetailAdapter = TechnicalDetailAdapter()
        technicalDetailsRecycler.apply {
            layoutManager = LinearLayoutManager(this@PduAnalysisActivity)
            adapter = technicalDetailAdapter
        }
    }

    private fun loadPduAnalysis() {
        val pduAnalysis = intent.getParcelableExtra<PduAnalysis>("pdu_analysis")
        pduAnalysis?.let { displayPduAnalysis(it) }
    }

    private fun displayPduAnalysis(analysis: PduAnalysis) {
        senderText.text = analysis.sender.ifEmpty { "Unknown" }
        messageText.text = analysis.message.ifEmpty { "No message content" }
        timestampText.text = dateFormat.format(analysis.timestamp)
        statusText.text = analysis.status
        pduTypeText.text = analysis.pduType
        encodingText.text = analysis.encoding
        messageLengthText.text = "${analysis.messageLength} characters"
        smscLengthText.text = "${analysis.smscLength} bytes"
        smscTypeText.text = analysis.smscType
        rawPduText.text = analysis.rawPdu
        
        // Set status color
        val statusColor = when (analysis.status) {
            "Valid" -> android.graphics.Color.GREEN
            "Invalid", "Error" -> android.graphics.Color.RED
            else -> android.graphics.Color.GRAY
        }
        statusText.setTextColor(statusColor)
        
        // Show/hide error message
        if (analysis.errorMessage.isNullOrEmpty()) {
            errorMessageText.visibility = android.view.View.GONE
        } else {
            errorMessageText.text = analysis.errorMessage
            errorMessageText.visibility = android.view.View.VISIBLE
        }
        
        // Load technical details
        technicalDetailAdapter.updateData(analysis.technicalDetails)
        
        // Set title
        supportActionBar?.title = "PDU Analysis - ${analysis.pduType}"
    }
}