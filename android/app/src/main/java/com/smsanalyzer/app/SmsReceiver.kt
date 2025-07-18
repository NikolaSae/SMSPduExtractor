package com.smsanalyzer.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.smsanalyzer.app.service.PduExtractionService

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                processSmsReceived(context, intent)
            }
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                processSmsDeliver(context, intent)
            }
            Telephony.Sms.Intents.SMS_CB_RECEIVED_ACTION -> {
                processCellBroadcast(context, intent)
            }
            Telephony.Sms.Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION -> {
                processEmergencyCellBroadcast(context, intent)
            }
            Telephony.Sms.Intents.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED_ACTION -> {
                processServiceCategoryProgramData(context, intent)
            }
            Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION -> {
                processWapPushIntent(context, intent)
            }
            else -> {
                Log.d(TAG, "Unknown intent action: ${intent.action}")
            }
        }
    }

    private fun processSmsReceived(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing SMS_RECEIVED")
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as? Array<*>
                val format = bundle.getString("format")

                pdus?.forEach { pdu ->
                    val pduByteArray = pdu as ByteArray
                    val smsMessage = SmsMessage.createFromPdu(pduByteArray, format)

                    if (smsMessage != null) {
                        val pduHex = bytesToHex(pduByteArray)
                        Log.d(TAG, "Received SMS PDU: $pduHex")

                        val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                            putExtra("pdu_hex", pduHex)
                            putExtra("sender", smsMessage.originatingAddress)
                            putExtra("message", smsMessage.messageBody)
                            putExtra("timestamp", smsMessage.timestampMillis)
                            putExtra("message_type", "SMS")
                        }
                        context.startService(serviceIntent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS_RECEIVED", e)
        }
    }

    private fun processSmsDeliver(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing SMS_DELIVER")
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as? Array<*>
                val format = bundle.getString("format")

                pdus?.forEach { pdu ->
                    val pduByteArray = pdu as ByteArray
                    val pduHex = bytesToHex(pduByteArray)
                    Log.d(TAG, "SMS_DELIVER PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "SMS_DELIVER")
                        putExtra("message", "SMS Deliver PDU")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "SMS_DELIVER")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS_DELIVER", e)
        }
    }

    private fun processCellBroadcast(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing Cell Broadcast (Bulk SMS)")
            val bundle = intent.extras
            if (bundle != null) {
                val cbMessage = bundle.get("message")
                val pduData = bundle.get("pdu") as? ByteArray

                if (pduData != null) {
                    val pduHex = bytesToHex(pduData)
                    Log.d(TAG, "Cell Broadcast PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Cell Broadcast")
                        putExtra("message", cbMessage?.toString() ?: "Cell Broadcast Message")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "CELL_BROADCAST")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Cell Broadcast", e)
        }
    }

    private fun processEmergencyCellBroadcast(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing Emergency Cell Broadcast")
            val bundle = intent.extras
            if (bundle != null) {
                val pduData = bundle.get("pdu") as? ByteArray
                
                if (pduData != null) {
                    val pduHex = bytesToHex(pduData)
                    Log.d(TAG, "Emergency Cell Broadcast PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Emergency Broadcast")
                        putExtra("message", "Emergency Cell Broadcast")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "EMERGENCY_BROADCAST")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Emergency Cell Broadcast", e)
        }
    }

    private fun processServiceCategoryProgramData(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing Service Category Program Data")
            val bundle = intent.extras
            if (bundle != null) {
                val pduData = bundle.get("pdu") as? ByteArray
                
                if (pduData != null) {
                    val pduHex = bytesToHex(pduData)
                    Log.d(TAG, "Service Category PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Service Category")
                        putExtra("message", "Service Category Program Data")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "SERVICE_CATEGORY")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Service Category Program Data", e)
        }
    }

    private fun processWapPushIntent(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing WAP Push message")
            val data = intent.getByteArrayExtra("data")
            val mimeType = intent.type

            if (data != null) {
                val pduHex = bytesToHex(data)
                Log.d(TAG, "WAP Push PDU: $pduHex")

                val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                    putExtra("pdu_hex", pduHex)
                    putExtra("sender", "WAP Push")
                    putExtra("message", "WAP Push Message (MIME: $mimeType)")
                    putExtra("timestamp", System.currentTimeMillis())
                    putExtra("message_type", "WAP_PUSH")
                }
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing WAP Push", e)
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }
}
