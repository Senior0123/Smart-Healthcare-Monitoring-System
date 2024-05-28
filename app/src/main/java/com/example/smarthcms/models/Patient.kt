package com.example.smarthcms.models

import android.os.Parcel
import android.os.Parcelable

data class Patient(
    val nationalId: String,
    val healthId: String,
    val phone: String,
    val dateOfBirth: String,
    val name: String,
    val email: String,
    val bloodType: String,
    val weight: String,
    val height: String,
    val image: String
) : Parcelable {

    constructor() : this("", "", "", "", "", "", "","","","")
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nationalId)
        parcel.writeString(healthId)
        parcel.writeString(phone)
        parcel.writeString(dateOfBirth)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(bloodType)
        parcel.writeString(weight)
        parcel.writeString(height)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Patient> {
        override fun createFromParcel(parcel: Parcel): Patient {
            return Patient(parcel)
        }

        override fun newArray(size: Int): Array<Patient?> {
            return arrayOfNulls(size)
        }
    }

    data class PatientForList(
        val nationalId: String,
        val name: String,
        val room: String,
        val image: String
    ) : Parcelable {
        constructor() : this("", "", "", "")
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(nationalId)
            parcel.writeString(name)
            parcel.writeString(room)
            parcel.writeString(image)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PatientForList> {
            override fun createFromParcel(parcel: Parcel): PatientForList {
                return PatientForList(parcel)
            }

            override fun newArray(size: Int): Array<PatientForList?> {
                return arrayOfNulls(size)
            }
        }

    }

    data class PatientDataForNurse(
        val name: String,
        val healthId: String,
        val bloodType: String,
        val age: String,
        val height: String,
        val weight:String
    ):Parcelable {
        constructor():this("","","","","","")
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(healthId)
            parcel.writeString(bloodType)
            parcel.writeString(age)
            parcel.writeString(height)
            parcel.writeString(weight)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PatientDataForNurse> {
            override fun createFromParcel(parcel: Parcel): PatientDataForNurse {
                return PatientDataForNurse(parcel)
            }

            override fun newArray(size: Int): Array<PatientDataForNurse?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class PatientData(val bloodType: String, val weight: String, val height: String):Parcelable {
        constructor():this("","","")
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        )


        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(bloodType)
            parcel.writeString(weight)
            parcel.writeString(height)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PatientData> {
            override fun createFromParcel(parcel: Parcel): PatientData {
                return PatientData(parcel)
            }

            override fun newArray(size: Int): Array<PatientData?> {
                return arrayOfNulls(size)
            }
        }
    }
}