package com.example.smarthcms.models

import android.os.Parcel
import android.os.Parcelable

data class Profile(
    val name: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val nationalId: String,
    val id: String,
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
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(dateOfBirth)
        parcel.writeString(nationalId)
        parcel.writeString(id)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }
}