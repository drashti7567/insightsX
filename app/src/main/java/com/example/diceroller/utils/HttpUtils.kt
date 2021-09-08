package com.example.diceroller.utils

import android.content.Context
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.entity.StringEntity

object HttpUtils {

    private const val BASE_URL = "http://localhost:8080/"

    private val client: AsyncHttpClient = AsyncHttpClient()

    fun get(url: String, params: RequestParams?, responseHandler: AsyncHttpResponseHandler?) {
        client.get(getAbsoluteUrl(url), params, responseHandler)
    }

    fun post(context: Context, url:String, entity: StringEntity, returnType: String, handler: AsyncHttpResponseHandler) {
        client.post(context, getAbsoluteUrl(url), entity, returnType, handler)
    }

    fun getByUrl(url: String?, params: RequestParams?, responseHandler: AsyncHttpResponseHandler?) {
        client.get(url, params, responseHandler)
    }

    fun postByUrl(url: String?, params: RequestParams?, responseHandler: AsyncHttpResponseHandler?) {
        client.post(url, params, responseHandler)
    }

    private fun getAbsoluteUrl(relativeUrl: String): String? {
        return BASE_URL + relativeUrl
    }
}