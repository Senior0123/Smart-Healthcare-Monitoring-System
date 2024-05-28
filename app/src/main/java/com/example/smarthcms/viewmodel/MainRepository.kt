package com.example.smarthcms.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.example.smarthcms.models.Admin
import com.example.smarthcms.models.Notification
import com.example.smarthcms.models.Nurse
import com.example.smarthcms.models.Patient
import com.example.smarthcms.models.Profile
import com.example.smarthcms.utils.Constants
import com.example.smarthcms.utils.EncryptionHelper
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainRepository(private val context: Context) {
    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "session"
    )
    private val encryptionHelper = EncryptionHelper("psw")
    private val db = Firebase.firestore
    private val storageRef = Firebase.storage.reference
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val database = FirebaseDatabase.getInstance()

    private val userKey = stringPreferencesKey("id")
    private val phoneKey = stringPreferencesKey("phone")
    private val typeKey = stringPreferencesKey("type")

    suspend fun saveId(id: String) {
        dataStore.edit {
            it[userKey] = id
        }
    }

    suspend fun getId(): String {
        val preferences = dataStore.data.first()
        return preferences[userKey] ?: ""
    }

    suspend fun savePhone(phone: String) {
        dataStore.edit {
            it[phoneKey] = phone
        }
    }

    suspend fun getPhone(): String {
        val preferences = dataStore.data.first()
        return preferences[phoneKey] ?: ""
    }

    suspend fun saveType(type: String) {
        dataStore.edit {
            it[typeKey] = type
        }
    }

    suspend fun getType(): String {
        val preferences = dataStore.data.first()
        return preferences[typeKey] ?: ""
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(userKey)
            preferences.remove(phoneKey)
            preferences.remove(typeKey)
        }
        auth.signOut()
    }

    fun createAccount(patient: Patient, password: String, callback: (String?) -> Unit) {
        val phoneQuery = db.collection("patients").whereEqualTo("phone", patient.phone)
        val emailQuery = db.collection("patients").whereEqualTo("email", patient.email)
        val nationalIdQuery =
            db.collection("patients").whereEqualTo("nationalId", patient.nationalId)

        phoneQuery.get().addOnSuccessListener { phoneQuerySnapshot ->
            if (!phoneQuerySnapshot.isEmpty) {
                callback("Phone already exists")
                return@addOnSuccessListener
            }

            emailQuery.get().addOnSuccessListener { emailQuerySnapshot ->
                if (!emailQuerySnapshot.isEmpty) {
                    callback("Email already exists")
                    return@addOnSuccessListener
                }

                nationalIdQuery.get().addOnSuccessListener { idQuerySnapshot ->
                    if (!idQuerySnapshot.isEmpty) {
                        callback("National Id already exists")
                        return@addOnSuccessListener
                    }

                    val nursePhoneQuery =
                        db.collection("nurses").whereEqualTo("phone", patient.phone)
                    val nurseEmailQuery =
                        db.collection("nurses").whereEqualTo("email", patient.email)
                    val nurseIdQuery =
                        db.collection("nurses").whereEqualTo("nationalId", patient.nationalId)

                    nursePhoneQuery.get().addOnSuccessListener { nursePhoneQuerySnapshot ->
                        if (!nursePhoneQuerySnapshot.isEmpty) {
                            callback("Phone already exists")
                            return@addOnSuccessListener
                        }

                        nurseEmailQuery.get().addOnSuccessListener { nurseEmailQuerySnapshot ->
                            if (!nurseEmailQuerySnapshot.isEmpty) {
                                callback("Email already exists for nurses")
                                return@addOnSuccessListener
                            }

                            nurseIdQuery.get().addOnSuccessListener { nurseIdQuerySnapshot ->
                                if (!nurseIdQuerySnapshot.isEmpty) {
                                    callback("National Id already exists")
                                    return@addOnSuccessListener
                                }

                                val adminPhoneQuery =
                                    db.collection("admins").whereEqualTo("phone", patient.phone)
                                val adminEmailQuery =
                                    db.collection("admins").whereEqualTo("email", patient.email)
                                val adminIdQuery = db.collection("admins")
                                    .whereEqualTo("nationalId", patient.nationalId)

                                adminPhoneQuery.get()
                                    .addOnSuccessListener { adminPhoneQuerySnapshot ->
                                        if (!adminPhoneQuerySnapshot.isEmpty) {
                                            callback("Phone already exists")
                                            return@addOnSuccessListener
                                        }

                                        adminEmailQuery.get()
                                            .addOnSuccessListener { adminEmailQuerySnapshot ->
                                                if (!adminEmailQuerySnapshot.isEmpty) {
                                                    callback("Email already exists for admins")
                                                    return@addOnSuccessListener
                                                }

                                                adminIdQuery.get()
                                                    .addOnSuccessListener { adminIdQuerySnapshot ->
                                                        if (!adminIdQuerySnapshot.isEmpty) {
                                                            callback("National Id already exists")
                                                            return@addOnSuccessListener
                                                        }

                                                        auth.createUserWithEmailAndPassword(
                                                            patient.email,
                                                            password
                                                        )
                                                            .addOnSuccessListener {
                                                                create(patient, callback)
                                                            }.addOnFailureListener { ex ->
                                                                callback(ex.localizedMessage)
                                                            }
                                                    }.addOnFailureListener { exception ->
                                                        callback(exception.localizedMessage)
                                                    }
                                            }.addOnFailureListener { exception ->
                                                callback(exception.localizedMessage)
                                            }
                                    }.addOnFailureListener { exception ->
                                        callback(exception.localizedMessage)
                                    }
                            }.addOnFailureListener { exception ->
                                callback(exception.localizedMessage)
                            }
                        }.addOnFailureListener { exception ->
                            callback(exception.localizedMessage)
                        }
                    }.addOnFailureListener { exception ->
                        callback(exception.localizedMessage)
                    }
                }.addOnFailureListener { exception ->
                    callback(exception.localizedMessage)
                }
            }.addOnFailureListener { exception ->
                callback(exception.localizedMessage)
            }
        }.addOnFailureListener { exception ->
            callback(exception.localizedMessage)
        }
    }


    private fun create(patient: Patient, callback: (String?) -> Unit) {
        val p = hashMapOf(
            "dateOfBirth" to patient.dateOfBirth,
            "email" to patient.email,
            "healthId" to patient.healthId,
            "image" to patient.image,
            "name" to patient.name,
            "nationalId" to patient.nationalId,
            "phone" to patient.phone,
        )
        db.collection("patients").add(p).addOnSuccessListener {
            callback("done")
        }.addOnFailureListener { exception ->
            callback(exception.localizedMessage)
        }
    }


    fun createNurse(nurse: Nurse, password: String, callback: (String?) -> Unit) {
        val phoneQuery = db.collection("nurses").whereEqualTo("phone", nurse.phone)
        val emailQuery = db.collection("nurses").whereEqualTo("email", nurse.email)

        phoneQuery.get().addOnSuccessListener { nursePhoneQuerySnapshot ->
            if (!nursePhoneQuerySnapshot.isEmpty) {
                callback("Phone already exists")
                return@addOnSuccessListener
            }

            emailQuery.get().addOnSuccessListener { nurseEmailQuerySnapshot ->
                if (!nurseEmailQuerySnapshot.isEmpty) {
                    callback("Email already exists")
                    return@addOnSuccessListener
                }

                val patientPhoneQuery = db.collection("patients").whereEqualTo("phone", nurse.phone)
                val patientEmailQuery = db.collection("patients").whereEqualTo("email", nurse.email)

                patientPhoneQuery.get().addOnSuccessListener { patientPhoneQuerySnapshot ->
                    if (!patientPhoneQuerySnapshot.isEmpty) {
                        callback("Phone already exists")
                        return@addOnSuccessListener
                    }

                    patientEmailQuery.get().addOnSuccessListener { patientEmailQuerySnapshot ->
                        if (!patientEmailQuerySnapshot.isEmpty) {
                            callback("Email already exists")
                            return@addOnSuccessListener
                        }

                        val adminPhoneQuery =
                            db.collection("admins").whereEqualTo("phone", nurse.phone)
                        val adminEmailQuery =
                            db.collection("admins").whereEqualTo("email", nurse.email)

                        adminPhoneQuery.get().addOnSuccessListener { adminPhoneQuerySnapshot ->
                            if (!adminPhoneQuerySnapshot.isEmpty) {
                                callback("Phone already exists")
                                return@addOnSuccessListener
                            }

                            adminEmailQuery.get().addOnSuccessListener { adminEmailQuerySnapshot ->
                                if (!adminEmailQuerySnapshot.isEmpty) {
                                    callback("Email already exists")
                                    return@addOnSuccessListener
                                }

                                val nurseQuery =
                                    db.collection("nurses")
                                        .whereEqualTo("nationalId", nurse.nationalId)
                                nurseQuery.get().addOnSuccessListener { nurseIdQuerySnapshot ->
                                    if (!nurseIdQuerySnapshot.isEmpty) {
                                        callback("National Id already exists")
                                        return@addOnSuccessListener
                                    }

                                    val patientIdQuery =
                                        db.collection("patients")
                                            .whereEqualTo("nationalId", nurse.nationalId)
                                    patientIdQuery.get()
                                        .addOnSuccessListener { patientIdQuerySnapshot ->
                                            if (!patientIdQuerySnapshot.isEmpty) {
                                                callback("National Id already exists")
                                                return@addOnSuccessListener
                                            }

                                            val adminIdQuery =
                                                db.collection("admins")
                                                    .whereEqualTo("nationalId", nurse.nationalId)
                                            adminIdQuery.get()
                                                .addOnSuccessListener { adminIdQuerySnapshot ->
                                                    if (!adminIdQuerySnapshot.isEmpty) {
                                                        callback("National Id already exists")
                                                        return@addOnSuccessListener
                                                    }

                                                    auth.createUserWithEmailAndPassword(
                                                        nurse.email,
                                                        password
                                                    )
                                                        .addOnSuccessListener {
                                                            newNurse(nurse, callback)
                                                        }.addOnFailureListener { ex ->
                                                            callback(ex.localizedMessage)
                                                        }
                                                }.addOnFailureListener { exception ->
                                                    callback(exception.localizedMessage)
                                                }
                                        }.addOnFailureListener { exception ->
                                            callback(exception.localizedMessage)
                                        }
                                }.addOnFailureListener { exception ->
                                    callback(exception.localizedMessage)
                                }
                            }.addOnFailureListener { exception ->
                                callback(exception.localizedMessage)
                            }
                        }.addOnFailureListener { exception ->
                            callback(exception.localizedMessage)
                        }
                    }.addOnFailureListener { exception ->
                        callback(exception.localizedMessage)
                    }
                }.addOnFailureListener { exception ->
                    callback(exception.localizedMessage)
                }
            }.addOnFailureListener { exception ->
                callback(exception.localizedMessage)
            }
        }.addOnFailureListener { exception ->
            callback(exception.localizedMessage)
        }
    }


    private fun newNurse(nurse: Nurse, callback: (String?) -> Unit) {
        val nurseMap = hashMapOf(
            "nationalId" to nurse.nationalId,
            "phone" to nurse.phone,
            "dateOfBirth" to nurse.dateOfBirth,
            "workId" to nurse.workId,
            "name" to nurse.name,
            "email" to nurse.email,
            "image" to nurse.image
        )
        db.collection("nurses").add(nurseMap).addOnSuccessListener {
            callback("done")
        }.addOnFailureListener { exception ->
            callback(exception.localizedMessage)
        }
    }


    fun login(
        nationalId: String,
        password: String,
        callback: (Patient?) -> Unit
    ) {
        db.collection("patients").whereEqualTo("nationalId", nationalId).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback(null)
                    return@addOnSuccessListener
                }
                val documentSnapshot = querySnapshot.documents[0]
                val patient = documentSnapshot.toObject(Patient::class.java)
                auth.signInWithEmailAndPassword(patient?.email ?: "", password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                                callback(patient)
                        }
                    }.addOnFailureListener {
                        callback(null)
                    }
            }.addOnFailureListener { exception ->
                callback(null)
            }
    }

    fun nurseLogin(
        nationalId: String,
        password: String,
        callback: (Nurse?) -> Unit
    ) {
        db.collection("nurses").whereEqualTo("nationalId", nationalId).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback(null)
                    return@addOnSuccessListener
                }
                val documentSnapshot = querySnapshot.documents[0]
                val nurse = documentSnapshot.toObject(Nurse::class.java)
                auth.signInWithEmailAndPassword(nurse?.email ?: "", password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(nurse)
                        }
                    }.addOnFailureListener {
                        callback(null)
                    }


            }.addOnFailureListener { exception ->
                callback(null)
            }
    }

    fun adminLogin(
        nationalId: String,
        password: String,
        callback: (Admin?) -> Unit
    ) {
        db.collection("admins").whereEqualTo("nationalId", nationalId).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback(null)
                    return@addOnSuccessListener
                }
                val documentSnapshot = querySnapshot.documents[0]
                val admin = documentSnapshot.toObject(Admin::class.java)
                auth.signInWithEmailAndPassword(admin?.email ?: "", password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                                callback(admin)
                        }
                    }.addOnFailureListener {
                        callback(null)
                    }
            }.addOnFailureListener { _ ->
                callback(null)
            }
    }


    fun getNursesForAdminHub(callback: (ArrayList<Nurse.NursesForAdminHub>?) -> Unit) {
        db.collection("nurses").addSnapshotListener { value, error ->
            if (error != null) {
                callback(null)
                return@addSnapshotListener
            }

            value?.let {
                val nursesList = ArrayList<Nurse.NursesForAdminHub>()
                for (doc in value) {
                    val workId = doc.getString("workId") ?: ""
                    val name = doc.getString("name") ?: ""
                    val image = doc.getString("image") ?: ""
                    val nurse = Nurse.NursesForAdminHub(workId, name, image)
                    nursesList.add(nurse)
                }
                callback(nursesList)
            }
        }
    }

    fun getPatients(callback: (ArrayList<Patient.PatientForList>?) -> Unit) {
        db.collection("patients").addSnapshotListener { patientsSnapshot, error ->
            if (error != null) {
                callback(null)
                return@addSnapshotListener
            }

            val allPatients = patientsSnapshot?.documents?.mapNotNull { doc ->
                val nationalId = doc.getString("nationalId") ?: ""
                val name = doc.getString("name") ?: ""
                val room = doc.getString("room") ?: "Room 01"
                val image = doc.getString("image") ?: ""
                Patient.PatientForList(nationalId, name, room, image)
            } ?: emptyList()

            db.collection("nurses").get().addOnSuccessListener { nursesSnapshot ->
                val excludedPatients = mutableSetOf<String>()
                nursesSnapshot.documents.forEach { nurseDoc ->
                    val patientListRef =
                        db.collection("nurses").document(nurseDoc.id).collection("patientList")
                    patientListRef.addSnapshotListener { value, error ->
                        val patientsInList = value?.documents?.mapNotNull { documentSnapshot ->
                            documentSnapshot.getString("nationalId")
                        } ?: emptyList()

                        excludedPatients.addAll(patientsInList)
                        val filteredPatients = allPatients.filter { patient ->
                            patient.nationalId !in excludedPatients
                        }

                        callback(ArrayList(filteredPatients))
                    }
                }
            }.addOnFailureListener {
                callback(null)
            }
        }
    }


    fun getPatientByNurseWorkId(
        workId: String,
        callback: (ArrayList<Patient.PatientForList>?) -> Unit
    ) {
        db.collection("nurses").whereEqualTo("workId", workId).limit(1).get().addOnSuccessListener {
            if (!it.isEmpty) {
                db.collection("nurses").document(it.documents[0].id).collection("patientList")
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            callback(null)
                            return@addSnapshotListener
                        }
                        value?.let {
                            val patientList = ArrayList<Patient.PatientForList>()
                            for (doc in value) {
                                val nationalId = doc.getString("nationalId") ?: ""
                                val name = doc.getString("name") ?: ""
                                val room = doc.getString("room") ?: ""
                                val image = doc.getString("image") ?: ""
                                patientList.add(
                                    Patient.PatientForList(
                                        nationalId,
                                        name,
                                        room,
                                        image
                                    )
                                )
                            }
                            callback(patientList)
                        }
                    }
            }
        }
    }

    fun getPatientByNurseNationalId(
        nationalId: String,
        callback: (ArrayList<Patient.PatientForList>?) -> Unit
    ) {
        db.collection("nurses").whereEqualTo("nationalId", nationalId).limit(1).get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    db.collection("nurses").document(it.documents[0].id).collection("patientList")
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                callback(null)
                                return@addSnapshotListener
                            }
                            value?.let {
                                val patientList = ArrayList<Patient.PatientForList>()
                                for (doc in value) {
                                    val patientId = doc.getString("nationalId") ?: ""
                                    val name = doc.getString("name") ?: ""
                                    val room = doc.getString("room") ?: ""
                                    val image = doc.getString("image") ?: ""
                                    patientList.add(
                                        Patient.PatientForList(
                                            patientId,
                                            name,
                                            room,
                                            image
                                        )
                                    )
                                }
                                callback(patientList)
                            }
                        }
                }
            }
    }

    fun getPatientDataForNurse(
        nationalId: String,
        callback: (Patient.PatientDataForNurse?) -> Unit
    ) {
        db.collection("patients")
            .whereEqualTo("nationalId", nationalId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val healthId = document.getString("healthId") ?: ""
                    val bloodType = document.getString("bloodType") ?: ""
                    val age = document.getString("age") ?: ""
                    val height = document.getString("height") ?: ""
                    val weight = document.getString("weight") ?: ""
                    callback(
                        Patient.PatientDataForNurse(
                            name,
                            healthId,
                            bloodType,
                            age,
                            height,
                            weight
                        )
                    )
                    return@addOnSuccessListener
                }
                callback(null)
            }
            .addOnFailureListener { exception ->
                Log.e("MDOT", "Error getting documents: ", exception)
                callback(null)
            }
    }

    fun getPatientData(
        nationalId: String,
        callback: (Patient.PatientData?) -> Unit
    ) {
        db.collection("patients")
            .whereEqualTo("nationalId", nationalId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val bloodType = document.getString("bloodType") ?: ""
                    val height = document.getString("height") ?: ""
                    val weight = document.getString("weight") ?: ""
                    callback(
                        Patient.PatientData(
                            bloodType,
                            weight,
                            height
                        )
                    )
                    return@addOnSuccessListener
                }
                callback(null)
            }
            .addOnFailureListener { exception ->
                Log.e("MDOT", "Error getting documents: ", exception)
                callback(null)
            }
    }


    fun addPatientToNurse(
        workId: String,
        patient: Patient.PatientForList,
        callback: (String?) -> Unit
    ) {
        val patientForList = hashMapOf(
            "nationalId" to patient.nationalId,
            "name" to patient.name,
            "room" to patient.room,
            "image" to patient.image
        )
        val nurseQuery = db.collection("nurses").whereEqualTo("workId", workId).limit(1)
        nurseQuery.get().addOnSuccessListener { nurseSnapshot ->
            if (!nurseSnapshot.isEmpty) {
                val nurseDocId = nurseSnapshot.documents[0].id
                val patientListQuery =
                    db.collection("nurses").document(nurseDocId).collection("patientList")
                        .whereEqualTo("nationalId", patient.nationalId)
                patientListQuery.get().addOnSuccessListener { patientSnapshot ->
                    if (patientSnapshot.isEmpty) {
                        db.collection("nurses").document(nurseDocId).collection("patientList")
                            .add(patientForList).addOnSuccessListener {
                                callback("done")
                            }
                    } else {
                        callback("Patient already exists in the list")
                    }
                }
            } else {
                callback("Nurse not found")
            }
        }
    }

    fun deletePatientFromList(
        workId: String,
        patientId: String,
        callback: (String?) -> Unit
    ) {
        val nurseQuery = db.collection("nurses").whereEqualTo("workId", workId).limit(1)
        nurseQuery.get().addOnSuccessListener { nurseSnapshot ->
            if (!nurseSnapshot.isEmpty) {
                val nurseDocId = nurseSnapshot.documents[0].id
                val patientQuery = db.collection("nurses").document(nurseDocId)
                    .collection("patientList").whereEqualTo("nationalId", patientId).limit(1)
                patientQuery.get().addOnSuccessListener { patientSnapshot ->
                    if (!patientSnapshot.isEmpty) {
                        val patientDocId = patientSnapshot.documents[0].id
                        db.collection("nurses").document(nurseDocId)
                            .collection("patientList").document(patientDocId)
                            .delete().addOnSuccessListener {
                                callback("done")
                            }.addOnFailureListener { e ->
                                callback("Error deleting patient: ${e.message}")
                            }
                    } else {
                        callback("Patient not found in the list")
                    }
                }
            } else {
                callback("Nurse not found")
            }
        }
    }


    fun updateNurse(nursesForUpdate: Nurse.NursesForUpdate, callback: (String?) -> Unit) {
        val phoneQuery = db.collection("nurses")
            .whereEqualTo("phone", nursesForUpdate.phone)
            .whereNotEqualTo("workId", nursesForUpdate.workId)

        phoneQuery.get().addOnSuccessListener { nursePhoneQuerySnapshot ->
            if (!nursePhoneQuerySnapshot.isEmpty) {
                callback("Phone already exists")
                return@addOnSuccessListener
            }

            val patientPhoneQuery =
                db.collection("patients").whereEqualTo("phone", nursesForUpdate.phone)
            patientPhoneQuery.get().addOnSuccessListener { patientPhoneQuerySnapshot ->
                if (!patientPhoneQuerySnapshot.isEmpty) {
                    callback("Phone already exists")
                    return@addOnSuccessListener
                }

                val adminPhoneQuery =
                    db.collection("admins").whereEqualTo("phone", nursesForUpdate.phone)
                adminPhoneQuery.get().addOnSuccessListener { adminPhoneQuerySnapshot ->
                    if (!adminPhoneQuerySnapshot.isEmpty) {
                        callback("Phone already exists")
                        return@addOnSuccessListener
                    }

                    db.collection("nurses").whereEqualTo("workId", nursesForUpdate.workId)
                        .limit(1).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.isEmpty) {
                                callback("Nurse not found")
                                return@addOnSuccessListener
                            }

                            val nurseDoc = querySnapshot.documents[0]
                            db.collection("nurses").document(nurseDoc.id)
                                .update(
                                    "phone", nursesForUpdate.phone,
                                    "email", nursesForUpdate.email,
                                    "password", nursesForUpdate.password
                                )
                                .addOnSuccessListener {
                                    callback("done")
                                }
                                .addOnFailureListener { e ->
                                    callback("Failed to update nurse: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            callback("Failed to fetch nurse: ${e.message}")
                        }
                }.addOnFailureListener { exception ->
                    callback(exception.localizedMessage)
                }
            }.addOnFailureListener { exception ->
                callback(exception.localizedMessage)
            }
        }.addOnFailureListener { exception ->
            callback(exception.localizedMessage)
        }
    }


    fun updatePatient(
        id: String,
        room: String?,
        height: String?,
        weight: String?,
        bloodType: String?,
        msg: (String?) -> Unit
    ) {
        val patientsCollection = db.collection("patients")

        val patientData = mutableMapOf<String, Any?>()
        if (room != null) patientData["room"] = room
        if (height != null) patientData["height"] = height
        if (weight != null) patientData["weight"] = weight
        if (bloodType != null) patientData["bloodType"] = bloodType

        patientsCollection
            .whereEqualTo("nationalId", id)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    patientsCollection.document(document.id)
                        .update(patientData)
                        .addOnSuccessListener {
                            msg("done")
                            updateRoom(id, room.toString())
                        }
                        .addOnFailureListener { e ->
                            msg("Failed to update patient: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                msg("Error getting documents: ${e.message}")
            }
    }

    private fun updateRoom(id: String, room: String) {
        db.collection("nurses").get().addOnSuccessListener {
            it.documents.forEach { doc ->
                db.collection("nurses").document(doc.id).collection("patientList")
                    .whereEqualTo("nationalId", id).limit(1).get().addOnSuccessListener { patient ->
                        db.collection("nurses").document(doc.id).collection("patientList")
                            .document(patient.documents[0].id).update("room", room)
                    }
            }
        }
    }

    private fun calculateAge(dateOfBirth: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateOfBirthDate = dateFormat.parse(dateOfBirth)
        val dobCalendar = Calendar.getInstance()
        if (dateOfBirthDate != null) {
            dobCalendar.time = dateOfBirthDate
        }

        val todayCalendar = Calendar.getInstance()
        var age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (dobCalendar.get(Calendar.MONTH) > todayCalendar.get(Calendar.MONTH) ||
            (dobCalendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                    dobCalendar.get(Calendar.DAY_OF_MONTH) > todayCalendar.get(Calendar.DAY_OF_MONTH))
        ) {
            age--
        }
        return age.toString()
    }


    fun getProfile(type: String, nationalId: String, profile: (Profile?) -> Unit) {
        db.collection(type).whereEqualTo("nationalId", nationalId).limit(1).get()
            .addOnSuccessListener {
                it.documents.forEach { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        if (type == "patients") {
                            profile(
                                Profile(
                                    documentSnapshot.getString("name") ?: "",
                                    documentSnapshot.getString("email") ?: "",
                                    documentSnapshot.getString("phone") ?: "",
                                    documentSnapshot.getString("dateOfBirth") ?: "",
                                    documentSnapshot.getString("nationalId") ?: "",
                                    documentSnapshot.getString("healthId") ?: "",
                                    documentSnapshot.getString("image") ?: ""
                                )
                            )
                        } else {
                            profile(
                                Profile(
                                    documentSnapshot.getString("name") ?: "",
                                    documentSnapshot.getString("email") ?: "",
                                    documentSnapshot.getString("phone") ?: "",
                                    documentSnapshot.getString("dateOfBirth") ?: "",
                                    documentSnapshot.getString("nationalId") ?: "",
                                    documentSnapshot.getString("workId") ?: "",
                                    documentSnapshot.getString("image") ?: ""
                                )
                            )
                        }
                    }
                }
            }


    }


    fun saveImage(imageUri: Uri?, collectionName: String, nationalId: String) {
        val imageRef = storageRef.child("images/${nationalId}.jpg")

        val uploadTask = imageUri?.let { imageRef.putFile(it) }
        uploadTask?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnCompleteListener { downloadTask ->
                    if (downloadTask.isSuccessful) {
                        val imageUrl = downloadTask.result.toString()
                        db.collection(collectionName).whereEqualTo("nationalId", nationalId)
                            .limit(1).get()
                            .addOnSuccessListener {
                                if (!it.isEmpty) {
                                    db.collection(collectionName).document(it.documents[0].id)
                                        .update("image", imageUrl)
                                }
                            }
                    }
                }
            }
        }
    }

    fun loadImage(collectionName: String, nationalId: String, callback: (String?) -> Unit) {
        db.collection(collectionName).whereEqualTo("nationalId", nationalId).limit(1).get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    db.collection(collectionName).document(it.documents[0].id)
                        .addSnapshotListener { value, error ->
                            value?.let { doc ->
                                val image = doc.getString("image")
                                if (!image.isNullOrEmpty()) {
                                    callback(image)
                                }
                            }
                        }
                }
            }
    }

    fun getHeartRate(callback: (String?) -> Unit) {
        val reference = database.getReference("data/Heart_Rate")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rate = snapshot.getValue(String::class.java)
                callback(rate)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        }

        reference.addValueEventListener(listener)
    }


    fun getOxygenRate(callback: (String?) -> Unit) {
        val reference = database.getReference("data/Heart_Rate")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rate = snapshot.getValue(String::class.java)
                callback(rate)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        }

        reference.addValueEventListener(listener)
    }

    fun getTemperature(callback: (String?) -> Unit) {
        val reference = database.getReference("data/Temperature")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rate = snapshot.getValue(String::class.java)
                callback(rate)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        }

        reference.addValueEventListener(listener)
    }

    fun pushNotification(msg: String, nationalId: String) {
        val reference = database.getReference("Notifications/msg")
        val flag = database.getReference("Notifications/flag")

        reference.setValue(msg).addOnSuccessListener {
            flag.setValue("1")
            saveNotification(msg, nationalId)
        }
    }

    fun getFlag(callback: (String?) -> Unit) {
        val flag = database.getReference("Notifications/flag")
        flag.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val f = snapshot.getValue(String::class.java)
                    callback(f)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }

        })
    }

    fun changeFlag() {
        val flag = database.getReference("Notifications/flag")
        flag.setValue("0")
    }

    fun notificationPrompt(callback: (String?) -> Unit) {
        val reference = database.getReference("Notifications/msg")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val msg = snapshot.getValue(String::class.java)
                    callback(msg)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }

        })
    }


    private fun saveNotification(msg: String, nationalId: String) {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        val formattedDate = String.format("%02d/%02d/%04d", day, month, year)
        val notification = hashMapOf(
            "msg" to msg,
            "nationalId" to nationalId,
            "date" to formattedDate
        )

        db.collection("Notifications").add(notification)
    }

    fun getNotifications(callback: (ArrayList<Notification>?) -> Unit) {
        db.collection("Notifications")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                value?.let { querySnapshot ->
                    val notifications = arrayListOf<Notification>()
                    for (doc in querySnapshot) {
                        val msg = doc.getString("msg") ?: ""
                        val nationalId = doc.getString("nationalId") ?: ""
                        db.collection("patients").whereEqualTo("nationalId", nationalId).limit(1)
                            .get()
                            .addOnSuccessListener { patientQuerySnapshot ->
                                if (!patientQuerySnapshot.isEmpty) {
                                    notifications.add(
                                        Notification(
                                            patientQuerySnapshot.documents[0].getString(
                                                "name"
                                            ).toString(),
                                            patientQuerySnapshot.documents[0].getString("room")
                                                ?: "Room 01",
                                            msg,
                                            patientQuerySnapshot.documents[0].getString("image")
                                                .toString()
                                        )
                                    )
                                }
                                callback(notifications)
                            }
                    }
                }
            }
    }

    fun removeNurse(workId: String?, function: (String?) -> Unit) {
        db.collection("nurses").whereEqualTo("workId", workId).limit(1).get().addOnSuccessListener {
            if (!it.isEmpty) {
                db.collection("nurses").document(it.documents[0].id).delete().addOnSuccessListener {
                    function("done")
                }.addOnFailureListener {
                    function(null)
                }
            } else {
                function(null)
            }
        }
    }


}