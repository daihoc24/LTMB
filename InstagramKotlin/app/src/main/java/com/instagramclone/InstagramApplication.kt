package com.instagramclone

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.instagramclone.core.network.Ipv4FirstDns
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Cache
import okhttp3.OkHttpClient

@HiltAndroidApp
class InstagramApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .okHttpClient {
            OkHttpClient.Builder()
                .dns(Ipv4FirstDns)
                .retryOnConnectionFailure(true)
                .cache(Cache(cacheDir.resolve("coil_http_cache"), 100L * 1024 * 1024))
                .build()
        }
        .build()
}
