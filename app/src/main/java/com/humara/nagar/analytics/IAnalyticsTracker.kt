package com.humara.nagar.analytics

import org.json.JSONObject

interface IAnalyticsTracker {
    /**
     * Log an event with a unique key and optional parameters
     */
    fun logEvent(event: String, properties: JSONObject?)
}