package com.instagramclone.core.network

import com.instagramclone.BuildConfig
import com.instagramclone.core.session.EncryptedSessionStore
import com.instagramclone.core.session.SessionCleanup
import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionStore
import com.instagramclone.data.repository.DefaultSessionRepository
import com.instagramclone.data.repository.DefaultAuthRepository
import com.instagramclone.feature.auth.domain.AuthRepository
import com.instagramclone.data.remote.user.UserApi
import com.instagramclone.data.remote.auth.AuthApi
import com.instagramclone.data.remote.auth.RegistrationApi
import com.instagramclone.data.remote.auth.VerificationApi
import com.instagramclone.data.remote.content.ContentApi
import com.instagramclone.data.repository.DefaultContentRepository
import com.instagramclone.feature.content.ContentRepository
import com.instagramclone.feature.social.SocialRepository
import com.instagramclone.data.repository.DefaultSocialRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import com.instagramclone.data.remote.social.SocialApi
import com.instagramclone.data.remote.social.CommentApi
import com.instagramclone.data.remote.notification.NotificationApi
import com.instagramclone.feature.notification.NotificationRepository
import com.instagramclone.data.repository.DefaultNotificationRepository
import com.instagramclone.data.remote.chat.ChatApi
import com.instagramclone.feature.chat.ChatRepository
import com.instagramclone.data.repository.DefaultChatRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionBindingModule {
    @Binds
    abstract fun bindSessionStore(implementation: EncryptedSessionStore): SessionStore

    @Binds
    abstract fun bindSessionRepository(implementation: DefaultSessionRepository): SessionRepository

    @Binds
    abstract fun bindAuthRepository(implementation: DefaultAuthRepository): AuthRepository

    @Binds
    abstract fun bindContentRepository(implementation: DefaultContentRepository): ContentRepository

    @Binds
    abstract fun bindSocialRepository(implementation: DefaultSocialRepository): SocialRepository

    @Binds
    abstract fun bindNotificationRepository(implementation: DefaultNotificationRepository): NotificationRepository

    @Binds
    abstract fun bindChatRepository(implementation: DefaultChatRepository): ChatRepository

    @Multibinds
    abstract fun sessionCleanupHooks(): Set<SessionCleanup>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .dns(Ipv4FirstDns)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.CORE_API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideVerificationApi(retrofit: Retrofit): VerificationApi =
        retrofit.create(VerificationApi::class.java)

    @Provides
    @Singleton
    fun provideRegistrationApi(retrofit: Retrofit): RegistrationApi =
        retrofit.create(RegistrationApi::class.java)

    @Provides
    @Singleton
    fun provideContentApi(retrofit: Retrofit): ContentApi = retrofit.create(ContentApi::class.java)

    @Provides @Singleton
    fun provideSocialApi(retrofit: Retrofit): SocialApi = retrofit.create(SocialApi::class.java)

    @Provides @Singleton @Named("realtime")
    fun provideRealtimeRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.REALTIME_API_BASE_URL).client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

    @Provides @Singleton
    fun provideCommentApi(@Named("realtime") retrofit: Retrofit): CommentApi = retrofit.create(CommentApi::class.java)

    @Provides @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides @Singleton
    fun provideChatApi(@Named("realtime") retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)
}
