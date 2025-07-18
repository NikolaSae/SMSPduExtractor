package com.smsanalyzer.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.smsanalyzer.app.adapter.PduAnalysisAdapter
import com.smsanalyzer.app.database.DatabaseHelper
import com.smsanalyzer.app.model.PduAnalysis

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PduAnalysisAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var emptyStateText: TextView

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
        private const val TAG = "MainActivity"
    }

    private val pduAnalysisReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.smsanalyzer.app.NEW_PDU_ANALYZED") {
                loadPduAnalyses()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupDatabase()
        checkPermissions()
        loadPduAnalyses()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_view)
        fab = findViewById(R.id.fab)
        emptyStateText = findViewById(R.id.text_empty_state)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PduAnalysisAdapter { pduAnalysis ->
            openPduAnalysis(pduAnalysis)
        }
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, PduAnalysisActivity::class.java))
        }
    }

    private fun setupDatabase() {
        databaseHelper = DatabaseHelper(this)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )

        val needsPermission = permissions.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (needsPermission) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                // Permissions granted
                loadPduAnalyses()
            } else {
                // Handle permission denied
                // Show dialog explaining why permissions are needed
            }
        }
    }

    private fun loadPduAnalyses() {
        val analyses = databaseHelper.getAllPduAnalyses()
        Log.d(TAG, "Loaded ${analyses.size} PDU analyses from database")
        adapter.updateData(analyses)
        
        // Show/hide empty state
        if (analyses.isEmpty()) {
            Log.d(TAG, "No analyses found, showing empty state")
            emptyStateText.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            Log.d(TAG, "Found analyses, showing list")
            emptyStateText.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun openPduAnalysis(pduAnalysis: PduAnalysis) {
        val intent = Intent(this, PduAnalysisActivity::class.java).apply {
            putExtra("pdu_analysis", pduAnalysis)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadPduAnalyses()
        
        // Register broadcast receiver for new PDU analyses
        val filter = IntentFilter("com.smsanalyzer.app.NEW_PDU_ANALYZED")
        registerReceiver(pduAnalysisReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        // Unregister broadcast receiver
        try {
            unregisterReceiver(pduAnalysisReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }
}