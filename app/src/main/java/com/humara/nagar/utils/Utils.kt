package com.humara.nagar.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
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

        fun getLongitudeAndLatitude(context: Context): List<Double> {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                val longitude = lastKnownLocation.longitude
                val latitude = lastKnownLocation.latitude
                return listOf(longitude, latitude)
            }
            return emptyList()
        }

        fun getAddressFromLongAndLat(
            context: Context,
        ): List<Address> {

            val longitudeAndLatitude = getLongitudeAndLatitude(context)
            if (longitudeAndLatitude.isNotEmpty()) {

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    longitudeAndLatitude[1],
                    longitudeAndLatitude[0],
                    1
                )

                if (addresses.isNotEmpty()) {
                    return addresses
                }
            }
            return emptyList()
        }

        fun formatImageString(imageTitle: String): String {
            return imageTitle
                .substring(0, 10) + ".." + imageTitle
                .substring(imageTitle.length - 3)
        }
    }
}