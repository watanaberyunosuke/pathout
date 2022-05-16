package com.monash.pathout.api

import okhttp3.OkHttpClient
import okhttp3.internal.and
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.Key
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PTVClient {
    val apiService: PTVEndpointInterface

    init {
        val ptvInterceptor = PTVInterceptor()
        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(ptvInterceptor).build()
        val retrofit = Retrofit.Builder().client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(PTVEndpointInterface::class.java)
    }

    fun getSignedUrl(uri: String): String {
        var url = ""
        try {
            url = buildTTAPIURL(uri)
        } catch (e: Exception) {
            // Error during signature calculation
        }

        return url
    }

    @Throws(Exception::class)
    private fun buildTTAPIURL(uri: String): String {
        val developerId = 3002096
        val privateKey = "13e68111-5649-4430-b15c-a86f61d8c010"
        val HMAC_SHA1_ALGORITHM = "HmacSHA1"
        val keyBytes = privateKey.toByteArray()
        val uriWithDeveloperID = uri + (if (uri.contains("?")) "&" else "?") +
                "devid=" + developerId
        val uriBytes = uriWithDeveloperID.toByteArray()
        val signingKey: Key = SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM)
        val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
        mac.init(signingKey)
        val signatureBytes = mac.doFinal(uriBytes)
        val signature = StringBuilder(signatureBytes.size * 2)

        for (signatureByte in signatureBytes) {
            val intVal: Int = signatureByte and 0xff
            if (intVal < 0x10) {
                signature.append("0")
            }
            signature.append(Integer.toHexString(intVal))
        }

        return SIGN_BASE_URL + uri + (if (uri.contains("?")) "&" else "?") +
                "devid=" + developerId + "&signature=" + signature.toString()
            .uppercase(Locale.getDefault())
    }

    companion object {
        const val BASE_URL = "http://timetableapi.ptv.vic.gov.au/v3/"
        const val SIGN_BASE_URL = "http://timetableapi.ptv.vic.gov.au"

        @JvmStatic
        @get:Synchronized
        var instance: PTVClient? = null
            get() {
                if (field == null) {
                    field = PTVClient()
                }
                return field
            }
            private set
    }
}