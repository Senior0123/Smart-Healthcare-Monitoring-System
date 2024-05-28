package com.example.smarthcms.models

import android.os.Parcel
import android.os.Parcelable

data class Admin(
    val name: String,
    val dateOfBirth: String,
    val email: String,
    val nationalId: String,
    val phone: String,
    val workId: String,
    val image: String
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
        parcel.writeString(dateOfBirth)
        parcel.writeString(email)
        parcel.writeString(nationalId)
        parcel.writeString(phone)
        parcel.writeString(workId)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Admin> {
        override fun createFromParcel(parcel: Parcel): Admin {
            return Admin(parcel)
        }

        override fun newArray(size: Int): Array<Admin?> {
            return arrayOfNulls(size)
        }
    }
}