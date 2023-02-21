package com.humara.nagar.utils

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import com.humara.nagar.constants.Constants
import java.util.*

/**
 * Utils class
 */
class Utils {
    companion object {

        fun getMobileNumberWithCountryCode(mobileNumber: String): String {
            return Constants.COUNTRY_CODE.plus(" $mobileNumber")
        }

        private fun getLongitudeAndLatitude(context: Context): Pair<Double, Double>? {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers: List<String> = locationManager.getProviders(true)

            var location: Location? = null

            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (location == null || l.accuracy < location.accuracy) {
                    location = l
                }
            }

            return location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                Pair(latitude, longitude)
            }
        }

        fun getAddressFromLongAndLat(
            context: Context,
        ): MutableList<Address>? {

            val longitudeAndLatitude = getLongitudeAndLatitude(context)
            if (longitudeAndLatitude != null) {

                val latitude = longitudeAndLatitude.first
                val longitude = longitudeAndLatitude.second

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses.isNotEmpty()) {
                    return addresses
                }
            }
            return null
        }

        fun formatImageString(imageTitle: String): String {
            return imageTitle
                .substring(0, 10) + ".." + imageTitle
                .substring(imageTitle.length - 3)
        }

        fun makeCallViaIntent(context: Context, phoneNumber: String) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            context.startActivity(intent)
        }
    }
}