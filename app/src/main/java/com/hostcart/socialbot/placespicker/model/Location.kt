package com.hostcart.socialbot.placespicker.model

import com.google.gson.annotations.SerializedName
import com.hostcart.socialbot.placespicker.model.LabeledLatLng

data class Location(
        @SerializedName("cc")
        val cc: String,
        @SerializedName("city")
        val city: String,
        @SerializedName("country")
        val country: String,
        @SerializedName("distance")
        val distance: Int,
        @SerializedName("formattedAddress")
        val formattedAddress: List<String>,
        @SerializedName("labeledLatLngs")
        val labeledLatLngs: List<LabeledLatLng>,
        @SerializedName("lat")
        val lat: Double,
        @SerializedName("lng")
        val lng: Double,
        @SerializedName("state")
        val state: String
)