package com.example.footballstats

import android.content.Context
import com.example.footballstats.core.datastore.appDataStore
import com.example.footballstats.core.network.ApiKeyInterceptor
import com.example.footballstats.core.network.SStatsApi
import com.example.footballstats.data.AuthRepository
import com.example.footballstats.data.LeaguesRepository
import com.example.footballstats.data.MatchesRepository
import com.example.footballstats.data.TeamsRepository
import com.example.footballstats.domain.AppConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface AppContainer {
    val config: AppConfig
    val matchesRepository: MatchesRepository
    val leaguesRepository: LeaguesRepository
    val teamsRepository: TeamsRepository
    val authRepository: AuthRepository
}

class DefaultAppContainer(
    private val context: Context
) : AppContainer {

    override val config: AppConfig = AppConfig(
        baseUrl = BuildConfig.SSTATS_BASE_URL,
        apiKey = BuildConfig.SSTATS_API_KEY
    )

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey = config.apiKey))
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val api: SStatsApi by lazy { retrofit.create(SStatsApi::class.java) }

    override val matchesRepository: MatchesRepository by lazy {
        MatchesRepository(
            api = api,
            dataStore = context.appDataStore,
            moshi = moshi,
        )
    }

    override val leaguesRepository: LeaguesRepository by lazy {
        LeaguesRepository(api = api)
    }

    override val teamsRepository: TeamsRepository by lazy {
        TeamsRepository(api = api)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(dataStore = context.appDataStore)
    }
}