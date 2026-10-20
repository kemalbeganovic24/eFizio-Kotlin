package com.efizio.efizio.data

data class Appointment(
    var id: String = "",
    val patientName: String = "",
    val service: String = "",
    val dateStr: String = "",
    val timeStr: String = "",
    val timestampMillis: Long = 0L,
    val priceKM: Int = 0
)
data class HidzamaRecord(
    val id: String = "",
    val sessionNumber: Int = 1,
    val cupsCount: Int = 10,
    val locations: List<String> = emptyList(),
    val timestampMillis: Long = System.currentTimeMillis()
)