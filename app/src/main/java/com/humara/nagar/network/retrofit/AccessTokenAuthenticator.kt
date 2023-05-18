package com.humara.nagar.network.retrofit

import com.humara.nagar.Logger
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.shared_pref.UserPreference
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Custom Retrofit Authenticator implementation for handling access token refresh.
 * This class is responsible for refreshing the access token when it expires and retrying the failed request with the updated token.
 *
 * @param userPreference The user preferences storage to retrieve and update access token and refresh token.
 * @param retrofit The RetrofitService instance used for making the token refresh request.
 */
class AccessTokenAuthenticator(private val userPreference: UserPreference, private val retrofit: RetrofitService) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val initialAccessToken = userPreference.token
        // Check if the request requires an access token or if an initial access token is available
        if (isRequestWithAccessToken(response).not() || initialAccessToken == null) {
            return null
        }
        synchronized(this) {
            val lastSavedAccessToken = userPreference.token
            // Check if the initial access token is the same as the last saved access token
            if (initialAccessToken == lastSavedAccessToken) {
                val tokenRefreshResponse = try {
                    retrofit.retrofitInstance.create(AuthenticationService::class.java)
                        .getAccessTokenFromRefreshToken(TokenRefreshRequest(userPreference.userId, userPreference.refreshToken))
                        .execute()
                } catch (e: Exception) {
                    Logger.debugLog("Exception while refresh token api call")
                    return null
                }
                if (tokenRefreshResponse.isSuccessful.not()) {
                    Logger.debugLog("token refresh failed")
                    return null
                } else {
                    tokenRefreshResponse.body()?.let {
                        Logger.debugLog("Token refresh success. New access token: ${it.token}")
                        userPreference.refreshToken = it.refreshToken
                        userPreference.token = it.token
                        return originalRequestWithUpdatedToken(response.request, it.token)
                    } ?: kotlin.run {
                        Logger.debugLog("empty body")
                        return null
                    }
                }
            } else {
                // Another parallel API call has already refreshed the token, return the original request with the last saved access token
                Logger.debugLog("Access token already refreshed by other parallel api call")
                return originalRequestWithUpdatedToken(response.request, lastSavedAccessToken)
            }
        }
    }

    /**
     * Checks if the request is using an access token.
     *
     * @param response The Response object received from the previous failed request.
     * @return True if the request has an "Authorization" header with a value starting with "Bearer", false otherwise.
     */
    private fun isRequestWithAccessToken(response: Response): Boolean {
        val header = response.request.header(NetworkConstants.NetworkHeaderConstants.AUTHORIZATION)
        return header != null && header.startsWith("Bearer")
    }

    /**
     * Creates a new request with the updated access token.
     *
     * @param request The original Request object.
     * @param accessToken The updated access token.
     * @return The new Request object with the updated access token.
     */
    private fun originalRequestWithUpdatedToken(request: Request, accessToken: String?): Request {
        val requestBuilder = request.newBuilder()
        accessToken?.let {
            requestBuilder.header(NetworkConstants.NetworkHeaderConstants.AUTHORIZATION, "Bearer $accessToken")
        }
        return requestBuilder.build()
    }
}