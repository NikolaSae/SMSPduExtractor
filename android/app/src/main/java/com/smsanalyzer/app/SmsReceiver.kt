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
        Log.d(TAG, "Intent extras: ${intent.extras?.keySet()?.joinToString(", ")}")
        
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                processSmsReceived(context, intent)
            }
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                processSmsDeliver(context, intent)
            }
            "android.provider.Telephony.SMS_RECEIVED_2" -> {
                processSmsReceived2(context, intent)
            }
            "android.provider.Telephony.SMS_CB_RECEIVED",
            "android.provider.Telephony.CBS_RECEIVED" -> {
                processCellBroadcast(context, intent)
            }
            "android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED",
            "android.provider.Telephony.ETWS_RECEIVED",
            "android.provider.Telephony.CMAS_RECEIVED" -> {
                processEmergencyCellBroadcast(context, intent)
            }
            "android.provider.Telephony.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED" -> {
                processServiceCategoryProgramData(context, intent)
            }
            "android.provider.Telephony.WAP_PUSH_RECEIVED" -> {
                processWapPushIntent(context, intent)
            }
            "android.provider.Telephony.MMS_RECEIVED" -> {
                processMmsReceived(context, intent)
            }
            "android.intent.action.DATA_SMS_RECEIVED" -> {
                processDataSmsReceived(context, intent)
            }
            "com.google.android.apps.messaging.shared.datamodel.action.RECEIVE_RCS_MESSAGE",
            "com.google.android.apps.messaging.shared.datamodel.action.INCOMING_RCS_MESSAGE",
            "com.google.android.apps.messaging.shared.datamodel.action.UPDATE_RCS_MESSAGE",
            "com.android.ims.RCS_MESSAGE_RECEIVED" -> {
                processRcsMessage(context, intent)
            }
            "com.android.mms.transaction.MESSAGE_SENT",
            "com.android.messaging.datamodel.action.RECEIVE_SMS" -> {
                processMessageSent(context, intent)
            }
            "android.telephony.action.SUBSCRIPTION_CARRIER_IDENTITY_CHANGED",
            "android.telephony.action.NETWORK_COUNTRY_CHANGED",
            "android.telephony.action.SIM_APPLICATION_STATE_CHANGED",
            "android.telephony.action.SERVICE_STATE_CHANGED" -> {
                processCarrierIdentityChanged(context, intent)
            }
            "android.intent.action.SIM_STATE_CHANGED",
            "android.telephony.action.DEFAULT_SMS_PACKAGE_CHANGED" -> {
                processSystemStateChanged(context, intent)
            }
            "android.intent.action.PROVIDER_CHANGED" -> {
                processProviderChanged(context, intent)
            }
            else -> {
                Log.d(TAG, "Unknown intent action: ${intent.action}")
                // Try to process any unknown intent as potential SMS data
                processGenericMessage(context, intent)
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
                // Log all available keys for debugging
                Log.d(TAG, "Cell Broadcast bundle keys: ${bundle.keySet()?.joinToString(", ")}")
                
                val cbMessage = bundle.get("message")
                val cbData = bundle.get("data") as? ByteArray
                val pduData = bundle.get("pdu") as? ByteArray
                val serialNumber = bundle.getInt("serial_number", -1)
                val serviceCategory = bundle.getInt("service_category", -1)
                val messageId = bundle.getInt("message_id", -1)
                val messageBody = bundle.getString("message_body")
                val languageCode = bundle.getString("language_code")

                Log.d(TAG, "Cell Broadcast - Serial: $serialNumber, Category: $serviceCategory, MsgID: $messageId")
                Log.d(TAG, "Cell Broadcast - Body: $messageBody, Language: $languageCode")

                // Try to get PDU data from multiple possible sources
                val actualPduData = pduData ?: cbData ?: run {
                    // If no direct PDU, try to extract from message object
                    val msg = cbMessage
                    if (msg != null) {
                        try {
                            // Try to access PDU through reflection if available
                            val clazz = msg.javaClass
                            val pduField = clazz.getDeclaredField("mPdu")
                            pduField.isAccessible = true
                            pduField.get(msg) as? ByteArray
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                if (actualPduData != null) {
                    val pduHex = bytesToHex(actualPduData)
                    Log.d(TAG, "Cell Broadcast PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Cell Broadcast")
                        putExtra("message", messageBody ?: cbMessage?.toString() ?: "Cell Broadcast Message")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "CELL_BROADCAST")
                        putExtra("serial_number", serialNumber)
                        putExtra("service_category", serviceCategory)
                        putExtra("message_id", messageId)
                        putExtra("language_code", languageCode)
                    }
                    context.startService(serviceIntent)
                } else {
                    Log.d(TAG, "No PDU data found in Cell Broadcast, creating synthetic entry")
                    // Create entry even without PDU data
                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", "NO_PDU_DATA")
                        putExtra("sender", "Cell Broadcast")
                        putExtra("message", messageBody ?: cbMessage?.toString() ?: "Cell Broadcast Message")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "CELL_BROADCAST")
                        putExtra("serial_number", serialNumber)
                        putExtra("service_category", serviceCategory)
                        putExtra("message_id", messageId)
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

    private fun processMmsReceived(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing MMS message")
            val bundle = intent.extras
            if (bundle != null) {
                val data = bundle.getByteArray("data")
                val contentLocation = bundle.getString("content_location")
                val transactionId = bundle.getString("transaction_id")
                
                if (data != null) {
                    val pduHex = bytesToHex(data)
                    Log.d(TAG, "MMS PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "MMS")
                        putExtra("message", "MMS Message (Location: $contentLocation)")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "MMS")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing MMS", e)
        }
    }

    private fun processDataSmsReceived(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing Data SMS")
            val bundle = intent.extras
            if (bundle != null) {
                val data = bundle.getByteArray("data")
                val originatingAddress = bundle.getString("originating_address")
                val destinationPort = bundle.getInt("destination_port", -1)
                
                if (data != null) {
                    val pduHex = bytesToHex(data)
                    Log.d(TAG, "Data SMS PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", originatingAddress ?: "Data SMS")
                        putExtra("message", "Data SMS (Port: $destinationPort)")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "DATA_SMS")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Data SMS", e)
        }
    }

    private fun processRcsMessage(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing RCS message: ${intent.action}")
            val bundle = intent.extras
            if (bundle != null) {
                // Log all available keys for debugging
                Log.d(TAG, "RCS bundle keys: ${bundle.keySet()?.joinToString(", ")}")
                
                val messageData = bundle.getByteArray("message_data")
                val data = bundle.getByteArray("data")
                val pduData = bundle.getByteArray("pdu")
                val content = bundle.getByteArray("content")
                val payload = bundle.getByteArray("payload")
                
                val conversationId = bundle.getString("conversation_id")
                val messageId = bundle.getString("message_id")
                val messageText = bundle.getString("message_text")
                val fromAddress = bundle.getString("from_address")
                val toAddress = bundle.getString("to_address")
                val messageType = bundle.getString("message_type")
                val contentType = bundle.getString("content_type")
                
                Log.d(TAG, "RCS - ConvID: $conversationId, MsgID: $messageId")
                Log.d(TAG, "RCS - From: $fromAddress, To: $toAddress")
                Log.d(TAG, "RCS - Type: $messageType, Content-Type: $contentType")
                Log.d(TAG, "RCS - Text: $messageText")

                // Try to get data from multiple possible sources
                val actualData = messageData ?: data ?: pduData ?: content ?: payload
                
                if (actualData != null) {
                    val pduHex = bytesToHex(actualData)
                    Log.d(TAG, "RCS message data: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", fromAddress ?: "RCS")
                        putExtra("message", messageText ?: "RCS Message (ID: $messageId)")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "RCS")
                        putExtra("conversation_id", conversationId)
                        putExtra("message_id", messageId)
                        putExtra("rcs_message_type", messageType)
                        putExtra("content_type", contentType)
                    }
                    context.startService(serviceIntent)
                } else {
                    Log.d(TAG, "No binary data found in RCS message, creating text-only entry")
                    // Create entry even without binary data
                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", "NO_BINARY_DATA")
                        putExtra("sender", fromAddress ?: "RCS")
                        putExtra("message", messageText ?: "RCS Message (ID: $messageId)")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "RCS")
                        putExtra("conversation_id", conversationId)
                        putExtra("message_id", messageId)
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing RCS message", e)
        }
    }

    private fun processMessageSent(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing message sent notification")
            val bundle = intent.extras
            if (bundle != null) {
                val messageUri = bundle.getString("message_uri")
                val messageId = bundle.getString("message_id")
                
                Log.d(TAG, "Message sent: URI=$messageUri, ID=$messageId")
                
                // Try to extract any PDU data from the intent
                val pduData = bundle.getByteArray("pdu") ?: bundle.getByteArray("data")
                if (pduData != null) {
                    val pduHex = bytesToHex(pduData)
                    
                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Sent Message")
                        putExtra("message", "Outgoing Message PDU")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "SENT")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message sent", e)
        }
    }

    private fun processCarrierIdentityChanged(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing carrier identity change")
            val bundle = intent.extras
            if (bundle != null) {
                val carrierId = bundle.getInt("carrier_id", -1)
                val subscriptionId = bundle.getInt("subscription_id", -1)
                
                Log.d(TAG, "Carrier identity changed: CarrierID=$carrierId, SubscriptionID=$subscriptionId")
                
                // This might contain configuration data
                val configData = bundle.getByteArray("config_data")
                if (configData != null) {
                    val pduHex = bytesToHex(configData)
                    
                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Carrier Config")
                        putExtra("message", "Carrier Identity Changed")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "CARRIER_CONFIG")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing carrier identity change", e)
        }
    }

    private fun processGenericMessage(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing generic message: ${intent.action}")
            val bundle = intent.extras
            if (bundle != null) {
                // Try to find any byte array data in the bundle
                for (key in bundle.keySet()) {
                    val value = bundle.get(key)
                    if (value is ByteArray && value.isNotEmpty()) {
                        val pduHex = bytesToHex(value)
                        Log.d(TAG, "Found data in key '$key': $pduHex")
                        
                        val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                            putExtra("pdu_hex", pduHex)
                            putExtra("sender", "Generic Message")
                            putExtra("message", "Generic Message Data (Key: $key)")
                            putExtra("timestamp", System.currentTimeMillis())
                            putExtra("message_type", "GENERIC")
                        }
                        context.startService(serviceIntent)
                        break // Only process the first byte array found
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing generic message", e)
        }
    }

    private fun processSmsReceived2(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing SMS_RECEIVED_2 (Enhanced SMS)")
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as? Array<*>
                val format = bundle.getString("format")
                val isMultipart = bundle.getBoolean("multipart", false)
                val totalParts = bundle.getInt("total_parts", 1)
                val currentPart = bundle.getInt("current_part", 1)

                Log.d(TAG, "Enhanced SMS - Multipart: $isMultipart, Part: $currentPart/$totalParts")

                pdus?.forEach { pdu ->
                    val pduByteArray = pdu as ByteArray
                    val pduHex = bytesToHex(pduByteArray)
                    Log.d(TAG, "Enhanced SMS PDU: $pduHex")

                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "Enhanced SMS")
                        putExtra("message", "Enhanced SMS Message (Part $currentPart/$totalParts)")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "ENHANCED_SMS")
                        putExtra("is_multipart", isMultipart)
                        putExtra("total_parts", totalParts)
                        putExtra("current_part", currentPart)
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Enhanced SMS", e)
        }
    }

    private fun processSystemStateChanged(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing System State Change: ${intent.action}")
            val bundle = intent.extras
            if (bundle != null) {
                val state = bundle.getString("ss")
                val simState = bundle.getString("sim_state")
                
                Log.d(TAG, "System state: $state, SIM state: $simState")
                
                // Check if there's any PDU data associated with state changes
                val pduData = bundle.getByteArray("pdu") ?: bundle.getByteArray("data")
                if (pduData != null) {
                    val pduHex = bytesToHex(pduData)
                    
                    val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                        putExtra("pdu_hex", pduHex)
                        putExtra("sender", "System State")
                        putExtra("message", "System State Change: ${intent.action}")
                        putExtra("timestamp", System.currentTimeMillis())
                        putExtra("message_type", "SYSTEM_STATE")
                    }
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing system state change", e)
        }
    }

    private fun processProviderChanged(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Processing Provider Change")
            val uri = intent.data
            val bundle = intent.extras
            
            Log.d(TAG, "Provider changed - URI: $uri")
            
            if (bundle != null) {
                // Check for any data that might contain PDUs
                for (key in bundle.keySet()) {
                    val value = bundle.get(key)
                    if (value is ByteArray && value.isNotEmpty()) {
                        val pduHex = bytesToHex(value)
                        Log.d(TAG, "Provider change data found in key '$key': $pduHex")
                        
                        val serviceIntent = Intent(context, PduExtractionService::class.java).apply {
                            putExtra("pdu_hex", pduHex)
                            putExtra("sender", "Provider Change")
                            putExtra("message", "Provider Change Data (URI: $uri)")
                            putExtra("timestamp", System.currentTimeMillis())
                            putExtra("message_type", "PROVIDER_CHANGE")
                        }
                        context.startService(serviceIntent)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing provider change", e)
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
