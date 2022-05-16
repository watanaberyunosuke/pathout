package com.monash.pathout.api

import com.monash.pathout.api.PTVClient.Companion.instance
import com.monash.pathout.api.PTVInterceptor
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class PTVInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Intercept the request, calculate the signed url and use it as the actual request
        var request: Request = chain.request()
        val uriPath = request.url.toUri().path
        val uriQuery = request.url.toUri().query
        val uri = "$uriPath?$uriQuery"
        val signedUrl = instance!!.getSignedUrl(uri)

        request = request.newBuilder().url(signedUrl).build()

        return chain.proceed(request)
    }

    companion object {
        private val TAG = PTVInterceptor::class.java.simpleName
    }
}