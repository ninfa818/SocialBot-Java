package com.hostcart.socialbot.model

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class TextStatus(@PrimaryKey var statusId: String = "", var text: String = "", var fontName: String = "", var backgroundColor: String = "") : RealmObject(), Parcelable {
    fun toMap(): Map<String, Any> {
        val map = hashMapOf<String, Any>()
        map.apply {
            put("text", text)
            put("fontName", fontName)
            put("backgroundColor", backgroundColor)
        }
        return map
    }


}
