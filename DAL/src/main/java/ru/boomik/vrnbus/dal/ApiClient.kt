package ru.boomik.vrnbus.dal

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    private var okBuilder: OkHttpClient.Builder
    private var adapterBuilder: Retrofit.Builder

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        okBuilder = OkHttpClient.Builder().addInterceptor(logging)
        val baseUrl = Consts.SERVER_URL
        adapterBuilder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return adapterBuilder
                .client(okBuilder.build())
                .build()
                .create(serviceClass)
    }
}