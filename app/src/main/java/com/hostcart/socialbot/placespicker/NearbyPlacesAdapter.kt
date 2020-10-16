package com.hostcart.socialbot.placespicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hostcart.socialbot.R
import com.hostcart.socialbot.utils.AppUtils
import com.hostcart.socialbot.utils.SharedPreferencesManager
import kotlinx.android.synthetic.main.row_place_dark.view.*
import kotlinx.android.synthetic.main.row_place_light.view.*

class NearbyPlacesAdapter(private val context: Context, private val places: List<Place>) : RecyclerView.Adapter<NearbyPlacesAdapter.NearbyPlacesHolder>() {
    var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): NearbyPlacesHolder {
        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            return NearbyPlacesHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_place_dark, parent, false))
        } else {
            return NearbyPlacesHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_place_light, parent, false))
        }
    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: NearbyPlacesHolder, position: Int) {
        holder.bind(places[position])
    }

    inner class NearbyPlacesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(place: Place) {
            if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
                itemView.tv_place_name_dark.text = place.name
                itemView.tv_place_address_dark.text = place.address
                Glide.with(context).load(place.iconUrl).into(itemView.icon_location_dark)
            } else {
                itemView.tv_place_name_light.text = place.name
                itemView.tv_place_address_light.text = place.address
                Glide.with(context).load(place.iconUrl).into(itemView.icon_location_light)
            }

            itemView.setOnClickListener {
                onClickListener?.onClick(it, place)
            }
        }
    }

    interface OnClickListener {
        fun onClick(view: View, place: Place)
    }
}