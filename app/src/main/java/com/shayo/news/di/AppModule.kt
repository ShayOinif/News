package com.shayo.news.di

import android.content.Context
import androidx.room.Room
import com.shayo.news.BuildConfig
import com.shayo.news.BuildConfig.NEWS_API_KEY
import com.shayo.news.db.ArticlesDao
import com.shayo.news.db.ArticlesDatabase
import com.shayo.news.network.NewsApiInterface
import com.shayo.news.utils.imageloader.ImageLoader
import com.shayo.news.utils.imageloader.ImageLoaderImpl
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideGsonFactory(): Converter.Factory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY //full log, please.
    }

    @Singleton
    @Provides
    fun provideAuthorizationInterceptor() = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            if (NEWS_API_KEY.isBlank()) return chain.proceed(originalRequest)

            //add the url to the original request:
            //add an authorization header as well:
            val new = originalRequest.newBuilder().url(originalRequest.url)
                .addHeader(NewsApiInterface.HEADER_AUTHORIZATION, NEWS_API_KEY)
                .build()

            return chain.proceed(new)
        }
    }

    @Singleton
    @Provides
    fun provideOKHTTPClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)

        if (BuildConfig.DEBUG) {
            client.addInterceptor(loggingInterceptor)
        }

        return client.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        httpClient: OkHttpClient,
        gsonConverterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(NewsApiInterface.BASE_URL)
        .addConverterFactory(gsonConverterFactory)
        .build()

    @Provides
    @Singleton
    fun provideNewsApi(retrofit: Retrofit): NewsApiInterface =
        retrofit.create(NewsApiInterface::class.java)

    @Provides
    fun provideArticlesDao(database: ArticlesDatabase): ArticlesDao {
        return database.articlesDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(
            appContext,
            ArticlesDatabase::class.java,
            ArticlesDatabase.DB_NAME
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providePicasso(): Picasso = Picasso.get()

    @Provides
    @Singleton
    fun provideImageLoader(picasso: Picasso): ImageLoader = ImageLoaderImpl(picasso)
}



