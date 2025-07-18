package com.smsanalyzer.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class PduAnalysis(
    val id: Long = 0,
    val rawPdu: String,
    val pduType: String,
    val sender: String,
    val message: String,
    val timestamp: Date,
    val encoding: String,
    val messageLength: Int,
    val smscLength: Int,
    val smscType: String,
    val senderLength: Int,
    val status: String,
    val errorMessage: String?,
    val technicalDetails: List<TechnicalDetail>,
    val createdAt: Date
) : Parcelable

@Parcelize
data class TechnicalDetail(
    val field: String,
    val value: String,
    val hex: String,
    val description: String
) : Parcelable

@Parcelize
data class PduBreakdown(
    val smscInfo: String,
    val pduType: String,
    val sender: String,
    val timestamp: String,
    val message: String
) : Parcelable