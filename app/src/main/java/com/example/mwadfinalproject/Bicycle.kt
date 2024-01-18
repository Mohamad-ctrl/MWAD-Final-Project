package com.example.mwadfinalproject

import android.graphics.ColorSpace.Model
import android.os.Parcel
import android.os.Parcelable

data class Bicycle(
    val id: String,
    val brand: String,
    val model: String,
    val description: String,
    val price: Int,
    val ImageURL: String,
    var addedToCart: Boolean,
    var quantity: Int
) : Parcelable {
    constructor() : this("", "", "", "", 0, "", false, 0)
    // Implement Parcelable methods here

    // Example constructor for Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(brand)
        parcel.writeString(model)
        parcel.writeString(description)
        parcel.writeInt(price)
        parcel.writeString(ImageURL)
        parcel.writeByte(if (addedToCart) 1 else 0)
        parcel.writeInt(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Bicycle> {
        override fun createFromParcel(parcel: Parcel): Bicycle {
            return Bicycle(parcel)
        }

        override fun newArray(size: Int): Array<Bicycle?> {
            return arrayOfNulls(size)
        }
    }
}
