package com.example.smarthcms.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.smarthcms.models.Admin
import com.example.smarthcms.models.Notification
import com.example.smarthcms.models.Nurse
import com.example.smarthcms.models.Patient
import com.example.smarthcms.models.Profile
import com.google.firebase.auth.MultiFactorResolver
import kotlinx.coroutines.launch

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {
    private val _nationalId = MutableLiveData<String>()
    val nationalId: LiveData<String> get() = _nationalId

    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> get() = _phone

    private val _type = MutableLiveData<String>()
    val type: LiveData<String> get() = _type

    private val _image = MutableLiveData<String?>()
    val image: MutableLiveData<String?> get() = _image


    private val _nursesForAdminHubResult = MutableLiveData<ArrayList<Nurse.NursesForAdminHub>?>()
    val nursesForAdminHubResult: LiveData<ArrayList<Nurse.NursesForAdminHub>?> get() = _nursesForAdminHubResult

    private val _patientsResult = MutableLiveData<ArrayList<Patient.PatientForList>?>()
    val patientsResult: LiveData<ArrayList<Patient.PatientForList>?> get() = _patientsResult

    private val _patientByNurseWorkIdResult = MutableLiveData<ArrayList<Patient.PatientForList>?>()
    val patientByNurseWorkIdResult: LiveData<ArrayList<Patient.PatientForList>?> get() = _patientByNurseWorkIdResult

    private val _patientByNurseNationalIdResult =
        MutableLiveData<ArrayList<Patient.PatientForList>?>()
    val patientByNurseNationalIdResult: LiveData<ArrayList<Patient.PatientForList>?> get() = _patientByNurseNationalIdResult

    private val _patientDataForNurseResult = MutableLiveData<Patient.PatientDataForNurse?>()
    val patientDataForNurseResult: LiveData<Patient.PatientDataForNurse?> get() = _patientDataForNurseResult

    private val _patientDataResult = MutableLiveData<Patient.PatientData?>()
    val patientDataResult: LiveData<Patient.PatientData?> get() = _patientDataResult

    private val _getProfileResult = MutableLiveData<Profile?>()
    val getProfileResult: LiveData<Profile?> get() = _getProfileResult

    private val _heartRateResult = MutableLiveData<String?>()
    val heartRateResult: LiveData<String?> get() = _heartRateResult

    private val _oxygenRateResult = MutableLiveData<String?>()
    val oxygenRateResult: LiveData<String?> get() = _oxygenRateResult

    private val _temperatureResult = MutableLiveData<String?>()
    val temperatureResult: LiveData<String?> get() = _temperatureResult

    private val _notificationsResult = MutableLiveData<ArrayList<Notification>?>()
    val notificationsResult: LiveData<ArrayList<Notification>?> get() = _notificationsResult

    private val _notificationPromptResult = MutableLiveData<String?>()
    val notificationPromptResult: LiveData<String?> get() = _notificationPromptResult

    private val _flagResult = MutableLiveData<String?>()
    val flagResult: LiveData<String?> get() = _flagResult


    init {
        _nationalId.value = ""
        _phone.value = ""
        _type.value = ""
        _image.value = ""
        _nursesForAdminHubResult.value = null
        _patientsResult.value = null
        _patientByNurseWorkIdResult.value = null
        _patientByNurseNationalIdResult.value = null
        _patientDataForNurseResult.value = null
        _patientDataResult.value = null
        _getProfileResult.value = null
        _heartRateResult.value = null
        _oxygenRateResult.value = null
        _temperatureResult.value = null
        _notificationsResult.value = null
        _notificationPromptResult.value = null
        _flagResult.value = null
    }


    fun saveId(id: String) {
        viewModelScope.launch {
            mainRepository.saveId(id)
            _nationalId.value = id
        }
    }

    fun getId() {
        viewModelScope.launch {
            _nationalId.value = mainRepository.getId()
        }
    }

    fun savePhone(phone: String) {
        viewModelScope.launch {
            mainRepository.savePhone(phone)
            _phone.value = phone
        }
    }

    fun getPhone() {
        viewModelScope.launch {
            _phone.value = mainRepository.getPhone()
        }
    }

    fun saveType(type: String) {
        viewModelScope.launch {
            mainRepository.saveType(type)
            _type.value = type
        }
    }

    fun getType() {
        viewModelScope.launch {
            _type.value = mainRepository.getType()
        }
    }

    fun logout() {
        viewModelScope.launch {
            mainRepository.logout()
        }
    }

    fun createAccount(patient: Patient,password: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            mainRepository.createAccount(patient,password) { result ->
                callback(result)
            }
        }
    }

    fun createNurse(nurse: Nurse,password: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            mainRepository.createNurse(nurse,password) { result ->
                callback(result)
            }
        }
    }

    fun login(nationalId: String, password: String, callback: (Patient?) -> Unit) {
        viewModelScope.launch {
            mainRepository.login(nationalId, password) { result ->
                callback(result)
            }
        }
    }

    fun nurseLogin(nationalId: String, password: String, callback: (Nurse?) -> Unit) {
        viewModelScope.launch {
            mainRepository.nurseLogin(nationalId, password) { result ->
                callback(result)
            }
        }
    }

    fun adminLogin(nationalId: String, password: String, callback: (Admin?) -> Unit) {
        viewModelScope.launch {
            mainRepository.adminLogin(nationalId, password){admin->
                callback(admin)
            }
        }
    }

    fun getNursesForAdminHub() {
        viewModelScope.launch {
            mainRepository.getNursesForAdminHub { result ->
                _nursesForAdminHubResult.value = result
            }
        }
    }

    fun getPatients() {
        viewModelScope.launch {
            mainRepository.getPatients { result ->
                _patientsResult.value = result
            }
        }
    }

    fun getPatientByNurseWorkId(workId: String) {
        viewModelScope.launch {
            mainRepository.getPatientByNurseWorkId(workId) { result ->
                _patientByNurseWorkIdResult.value = result
            }
        }
    }

    fun getPatientByNurseNationalId(nationalId: String) {
        viewModelScope.launch {
            mainRepository.getPatientByNurseNationalId(nationalId) { result ->
                _patientByNurseNationalIdResult.value = result
            }
        }
    }

    fun getPatientDataForNurse(nationalId: String) {
        viewModelScope.launch {
            mainRepository.getPatientDataForNurse(nationalId) { result ->
                _patientDataForNurseResult.value = result
            }
        }
    }

    fun getPatientData(nationalId: String) {
        viewModelScope.launch {
            mainRepository.getPatientData(nationalId) { result ->
                _patientDataResult.value = result
            }
        }
    }

    fun addPatientToNurse(workId: String, patient: Patient.PatientForList,callback: (String?) -> Unit) {
        viewModelScope.launch {
            mainRepository.addPatientToNurse(workId, patient) { result ->
                callback(result)
            }
        }
    }

    fun deletePatientFromList(workId: String, patientId: String,callback: (String?) -> Unit) {
        viewModelScope.launch {
            mainRepository.deletePatientFromList(workId, patientId) { result ->
                callback(result)
            }
        }
    }

    fun updateNurse(nursesForUpdate: Nurse.NursesForUpdate,callback: (String?) -> Unit) {
        viewModelScope.launch {
            mainRepository.updateNurse(nursesForUpdate) { result ->
                callback(result)
            }
        }
    }

    fun updatePatient(id: String, room: String, height: String, weight: String, bloodType: String,callback: (String?) -> Unit) {
        viewModelScope.launch {
            mainRepository.updatePatient(id, room, height, weight, bloodType) { result ->
                callback(result)
            }
        }
    }

    fun getProfile(type: String, nationalId: String) {
        viewModelScope.launch {
            mainRepository.getProfile(type, nationalId) { result ->
                _getProfileResult.value = result
            }
        }
    }

    fun saveImage(imageUri: Uri?, collectionName: String, nationalId: String) {
        viewModelScope.launch {
            mainRepository.saveImage(imageUri, collectionName, nationalId)
        }
    }

    fun loadImage(collectionName: String, nationalId: String) {
        viewModelScope.launch {
            mainRepository.loadImage(collectionName, nationalId) { result ->
                _image.value = result
            }
        }
    }

    fun getHeartRate() {
        viewModelScope.launch {
            mainRepository.getHeartRate { result ->
                _heartRateResult.value = result
            }
        }
    }

    fun getOxygenRate() {
        viewModelScope.launch {
            mainRepository.getOxygenRate { result ->
                _oxygenRateResult.value = result
            }
        }
    }

    fun getTemperature() {
        viewModelScope.launch {
            mainRepository.getTemperature { result ->
                _temperatureResult.value = result
            }
        }
    }

    fun pushNotification(msg: String, nationalId: String) {
        viewModelScope.launch {
            mainRepository.pushNotification(msg, nationalId)
        }
    }

    fun getFlag() {
        viewModelScope.launch {
            mainRepository.getFlag { result ->
                _flagResult.value = result
            }
        }
    }

    fun changeFlag() {
        viewModelScope.launch {
            mainRepository.changeFlag()
        }
    }

    fun getNotifications() {
        viewModelScope.launch {
            mainRepository.getNotifications { result ->
                _notificationsResult.value = result
            }
        }
    }

    fun notificationPrompt() {
        viewModelScope.launch {
            mainRepository.notificationPrompt { result ->
                _notificationPromptResult.value = result
            }
        }
    }

    fun removeNurse(workId: String?, function: (String?) -> Unit) {
        mainRepository.removeNurse(workId){
            function(it)
        }
    }

}

class MainViewModelFactory(private val mainRepository: MainRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mainRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}