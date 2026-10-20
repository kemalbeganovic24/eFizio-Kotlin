package com.efizio.efizio.data

import com.google.firebase.firestore.FirebaseFirestore

class FizioRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. ZAKAŽI TERMIN + AUTOMATSKI KARTON AKO JE HIDŽAMA
    fun addAppointmentWithHidzama(
        appointment: Appointment,
        isHidzama: Boolean,
        cupsCount: Int,
        locations: List<String>,
        onComplete: () -> Unit
    ) {
        // TABELA 1: Upisujemo u 'appointments' (za kalendar i notifikacije)
        val apptRef = db.collection("appointments").document()
        val apptWithId = appointment.copy(id = apptRef.id)

        apptRef.set(apptWithId).addOnSuccessListener {

            // TABELA 2: Ako je štiklirana Hidžama, pravimo POTPUNO NEZAVISAN karton u 'hidzama_records'
            if (isHidzama) {
                val hidzamaZapis = hashMapOf(
                    "patientName" to appointment.patientName,
                    "dateStr" to appointment.dateStr,
                    "timestampMillis" to appointment.timestampMillis,
                    "cupsCount" to cupsCount,
                    "locations" to locations,
                    "service" to appointment.service,
                    "priceKM" to appointment.priceKM
                )

                // Upis u drugu tabelu
                db.collection("hidzama_records")
                    .add(hidzamaZapis)
                    .addOnCompleteListener {
                        onComplete()
                    }
            } else {
                onComplete()
            }
        }
    }

    // 2. SLUŠANJE TERMINA UŽIVO (Za HomeView)
    fun getAppointmentsSnapshot(onUpdate: (List<Appointment>) -> Unit) {
        db.collection("appointments")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val appt = doc.toObject(Appointment::class.java)
                    appt?.copy(id = doc.id)
                }
                onUpdate(list)
            }
    }

    // 3. BRISANJE TERMINA (Briše samo iz kalendara, karton u hidzama_records OSTAJE!)
    fun deleteAppointment(appointmentId: String, onComplete: (() -> Unit)? = null) {
        db.collection("appointments")
            .document(appointmentId)
            .delete()
            .addOnSuccessListener {
                onComplete?.invoke()
            }
    }

    // 4. PRETRAGA KARTONA (Čita iz trajne baze 'hidzama_records')
    fun getHidzamaRecordsByPatient(patientNameQuery: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("hidzama_records")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    data?.put("id", doc.id)
                    data
                }.filter {
                    val name = it["patientName"] as? String ?: ""
                    name.contains(patientNameQuery, ignoreCase = true)
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    // 5. AŽURIRANJE DATUMA HIDŽAMA KARTONA
    fun updateHidzamaRecordDate(
        recordId: String,
        newDateStr: String,
        newTimestampMillis: Long,
        onComplete: () -> Unit
    ) {
        db.collection("hidzama_records")
            .document(recordId)
            .update(
                mapOf(
                    "dateStr" to newDateStr,
                    "timestampMillis" to newTimestampMillis
                )
            )
            .addOnSuccessListener {
                onComplete()
            }
    }
}