package com.humara.nagar.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.humara.nagar.Logger
import com.humara.nagar.constants.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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
        fun saveMediaToStorage(bitmap: Bitmap, context: Context): Boolean {
            var success = false
            val filename = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Logger.debugLog("Saved to Photos")
                success = true
            }
            return success
        }

        fun toStringWithoutSpaces(inputString: String) : String {
            val stringBuilder = StringBuilder()
            for (char in inputString.toCharArray())
                if (char.isDigit() or char.isLetter())
                    stringBuilder.append(char)
            return stringBuilder.toString()
        }

        fun showDotStringAfterLimitReached(length: Int, string: String) : String {
            val stringBuilder = StringBuilder()
            Logger.debugLog("string: $string")
           for (i in 0 until length) {
                stringBuilder.append(string[i])
           }

            return stringBuilder.append("...").toString()
        }
    }
}