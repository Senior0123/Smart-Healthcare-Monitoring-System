package com.example.smarthcms.models

import android.os.Parcel
import android.os.Parcelable

data class Nurse(
    val name: String,
    val workId: String,
    val nationalId: String,
    val email: String,
    val dateOfBirth: String,
    val phone: String,
    val image:String
) : Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
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
        parcel.writeString(workId)
        parcel.writeString(image)
        parcel.writeString(nationalId)
        parcel.writeString(email)
        parcel.writeString(dateOfBirth)
        parcel.writeString(phone)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Nurse> {
        override fun createFromParcel(parcel: Parcel): Nurse {
            return Nurse(parcel)
        }

        override fun newArray(size: Int): Array<Nurse?> {
            return arrayOfNulls(size)
        }
    }

    data class NursesForAdminHub(val workId: String, val name: String,val image: String):Parcelable {
        constructor():this("","","")
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(workId)
            parcel.writeString(name)
            parcel.writeString(image)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<NursesForAdminHub> {
            override fun createFromParcel(parcel: Parcel): NursesForAdminHub {
                return NursesForAdminHub(parcel)
            }

            override fun newArray(size: Int): Array<NursesForAdminHub?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class NursesForUpdate(
        val workId: String,
        val name: String,
        val password: String,
        val phone: String,
        val email: String
    ):Parcelable{
        constructor():this(
            "",
            "",
            "",
            "",
            ""
        )
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(workId)
            parcel.writeString(name)
            parcel.writeString(password)
            parcel.writeString(phone)
            parcel.writeString(email)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<NursesForUpdate> {
            override fun createFromParcel(parcel: Parcel): NursesForUpdate {
                return NursesForUpdate(parcel)
            }

            override fun newArray(size: Int): Array<NursesForUpdate?> {
                return arrayOfNulls(size)
            }
        }

    }
}
