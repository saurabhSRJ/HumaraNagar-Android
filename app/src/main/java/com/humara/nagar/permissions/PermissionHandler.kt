package com.humara.nagar.permissions

interface PermissionHandler {
    /**
     * Callback to be invoked when all the requested permissions are granted
     */
    fun onPermissionGranted()

    /**
     * Callback to be invoked when the requested permission is denied and permission is NOT absolutely necessary
     * @param permissions: List of permissions which were denied
     */
    fun onPermissionDenied(permissions: List<String>)
}