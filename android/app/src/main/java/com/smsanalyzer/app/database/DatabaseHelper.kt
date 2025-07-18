package com.smsanalyzer.app.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smsanalyzer.app.model.PduAnalysis
import com.smsanalyzer.app.model.TechnicalDetail
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sms_pdu_analyzer.db"
        private const val DATABASE_VERSION = 1
        
        private const val TABLE_PDU_ANALYSIS = "pdu_analysis"
        private const val COLUMN_ID = "id"
        private const val COLUMN_RAW_PDU = "raw_pdu"
        private const val COLUMN_PDU_TYPE = "pdu_type"
        private const val COLUMN_SENDER = "sender"
        private const val COLUMN_MESSAGE = "message"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_ENCODING = "encoding"
        private const val COLUMN_MESSAGE_LENGTH = "message_length"
        private const val COLUMN_SMSC_LENGTH = "smsc_length"
        private const val COLUMN_SMSC_TYPE = "smsc_type"
        private const val COLUMN_SENDER_LENGTH = "sender_length"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_ERROR_MESSAGE = "error_message"
        private const val COLUMN_TECHNICAL_DETAILS = "technical_details"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PDU_ANALYSIS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RAW_PDU TEXT NOT NULL,
                $COLUMN_PDU_TYPE TEXT,
                $COLUMN_SENDER TEXT,
                $COLUMN_MESSAGE TEXT,
                $COLUMN_TIMESTAMP TEXT,
                $COLUMN_ENCODING TEXT,
                $COLUMN_MESSAGE_LENGTH INTEGER,
                $COLUMN_SMSC_LENGTH INTEGER,
                $COLUMN_SMSC_TYPE TEXT,
                $COLUMN_SENDER_LENGTH INTEGER,
                $COLUMN_STATUS TEXT,
                $COLUMN_ERROR_MESSAGE TEXT,
                $COLUMN_TECHNICAL_DETAILS TEXT,
                $COLUMN_CREATED_AT TEXT
            )
        """.trimIndent()
        
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PDU_ANALYSIS")
        onCreate(db)
    }

    fun insertPduAnalysis(pduAnalysis: PduAnalysis): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RAW_PDU, pduAnalysis.rawPdu)
            put(COLUMN_PDU_TYPE, pduAnalysis.pduType)
            put(COLUMN_SENDER, pduAnalysis.sender)
            put(COLUMN_MESSAGE, pduAnalysis.message)
            put(COLUMN_TIMESTAMP, dateFormat.format(pduAnalysis.timestamp))
            put(COLUMN_ENCODING, pduAnalysis.encoding)
            put(COLUMN_MESSAGE_LENGTH, pduAnalysis.messageLength)
            put(COLUMN_SMSC_LENGTH, pduAnalysis.smscLength)
            put(COLUMN_SMSC_TYPE, pduAnalysis.smscType)
            put(COLUMN_SENDER_LENGTH, pduAnalysis.senderLength)
            put(COLUMN_STATUS, pduAnalysis.status)
            put(COLUMN_ERROR_MESSAGE, pduAnalysis.errorMessage)
            put(COLUMN_TECHNICAL_DETAILS, gson.toJson(pduAnalysis.technicalDetails))
            put(COLUMN_CREATED_AT, dateFormat.format(pduAnalysis.createdAt))
        }
        
        return db.insert(TABLE_PDU_ANALYSIS, null, values)
    }

    fun getAllPduAnalyses(): List<PduAnalysis> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PDU_ANALYSIS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )
        
        val analyses = mutableListOf<PduAnalysis>()
        
        cursor.use {
            while (it.moveToNext()) {
                val technicalDetailsJson = it.getString(it.getColumnIndexOrThrow(COLUMN_TECHNICAL_DETAILS))
                val technicalDetails = if (technicalDetailsJson.isNullOrEmpty()) {
                    emptyList()
                } else {
                    gson.fromJson<List<TechnicalDetail>>(
                        technicalDetailsJson,
                        object : TypeToken<List<TechnicalDetail>>() {}.type
                    )
                }
                
                val timestampString = it.getString(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val createdAtString = it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                
                val pduAnalysis = PduAnalysis(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    rawPdu = it.getString(it.getColumnIndexOrThrow(COLUMN_RAW_PDU)),
                    pduType = it.getString(it.getColumnIndexOrThrow(COLUMN_PDU_TYPE)),
                    sender = it.getString(it.getColumnIndexOrThrow(COLUMN_SENDER)),
                    message = it.getString(it.getColumnIndexOrThrow(COLUMN_MESSAGE)),
                    timestamp = parseDate(timestampString),
                    encoding = it.getString(it.getColumnIndexOrThrow(COLUMN_ENCODING)),
                    messageLength = it.getInt(it.getColumnIndexOrThrow(COLUMN_MESSAGE_LENGTH)),
                    smscLength = it.getInt(it.getColumnIndexOrThrow(COLUMN_SMSC_LENGTH)),
                    smscType = it.getString(it.getColumnIndexOrThrow(COLUMN_SMSC_TYPE)),
                    senderLength = it.getInt(it.getColumnIndexOrThrow(COLUMN_SENDER_LENGTH)),
                    status = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS)),
                    errorMessage = it.getString(it.getColumnIndexOrThrow(COLUMN_ERROR_MESSAGE)),
                    technicalDetails = technicalDetails,
                    createdAt = parseDate(createdAtString)
                )
                
                analyses.add(pduAnalysis)
            }
        }
        
        return analyses
    }

    fun getPduAnalysisById(id: Long): PduAnalysis? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PDU_ANALYSIS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                val technicalDetailsJson = it.getString(it.getColumnIndexOrThrow(COLUMN_TECHNICAL_DETAILS))
                val technicalDetails = if (technicalDetailsJson.isNullOrEmpty()) {
                    emptyList()
                } else {
                    gson.fromJson<List<TechnicalDetail>>(
                        technicalDetailsJson,
                        object : TypeToken<List<TechnicalDetail>>() {}.type
                    )
                }
                
                val timestampString = it.getString(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val createdAtString = it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                
                return PduAnalysis(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    rawPdu = it.getString(it.getColumnIndexOrThrow(COLUMN_RAW_PDU)),
                    pduType = it.getString(it.getColumnIndexOrThrow(COLUMN_PDU_TYPE)),
                    sender = it.getString(it.getColumnIndexOrThrow(COLUMN_SENDER)),
                    message = it.getString(it.getColumnIndexOrThrow(COLUMN_MESSAGE)),
                    timestamp = parseDate(timestampString),
                    encoding = it.getString(it.getColumnIndexOrThrow(COLUMN_ENCODING)),
                    messageLength = it.getInt(it.getColumnIndexOrThrow(COLUMN_MESSAGE_LENGTH)),
                    smscLength = it.getInt(it.getColumnIndexOrThrow(COLUMN_SMSC_LENGTH)),
                    smscType = it.getString(it.getColumnIndexOrThrow(COLUMN_SMSC_TYPE)),
                    senderLength = it.getInt(it.getColumnIndexOrThrow(COLUMN_SENDER_LENGTH)),
                    status = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS)),
                    errorMessage = it.getString(it.getColumnIndexOrThrow(COLUMN_ERROR_MESSAGE)),
                    technicalDetails = technicalDetails,
                    createdAt = parseDate(createdAtString)
                )
            }
        }
        
        return null
    }

    fun deletePduAnalysis(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_PDU_ANALYSIS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun clearAllPduAnalyses(): Int {
        val db = writableDatabase
        return db.delete(TABLE_PDU_ANALYSIS, null, null)
    }

    private fun parseDate(dateString: String?): Date {
        return try {
            if (dateString.isNullOrEmpty()) {
                Date()
            } else {
                dateFormat.parse(dateString) ?: Date()
            }
        } catch (e: Exception) {
            Date()
        }
    }
}