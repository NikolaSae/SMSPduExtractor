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
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            try {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as Array<*>
                    val format = bundle.getString("format")

                    for (pdu in pdus) {
                        val pduByteArray = pdu as ByteArray
                        val smsMessage = SmsMessage.createFromPdu(pduByteArray, format)

                        if (smsMessage != null) {
                            val pduHex = bytesToHex(pduByteArray)
                            Log.d(TAG, "Received SMS PDU: $pduHex")

                            // Start service to extract and analyze PDU
                            val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                                putExtra("pdu_hex", pduHex)
                                putExtra("sender", smsMessage.originatingAddress)
                                putExtra("message", smsMessage.messageBody)
                                putExtra("timestamp", smsMessage.timestampMillis)
                            }
                            context.startService(serviceIntent)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS", e)
            }
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
