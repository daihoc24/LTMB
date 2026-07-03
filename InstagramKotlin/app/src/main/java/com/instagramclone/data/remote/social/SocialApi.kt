package com.instagramclone.data.remote.social

import com.instagramclone.core.network.ApiEnvelope
import com.instagramclone.core.network.AuthRequired
import com.instagramclone.data.remote.content.PostDto
import com.instagramclone.data.remote.user.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag

interface SocialApi {
    @POST("v1/post/postOfUsernameWithUser") suspend fun postsOfUser(@Body body: UsernameRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<List<PostDto>>>
    @GET("v1/search/username") suspend fun searchUsers(@Query("content") content: String, @Query("id") id: String, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<List<UserResponseDto>>>
    @GET("v1/search/caption") suspend fun searchPosts(@Query("content") content: String, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<List<PostDto>>>
    @POST("v1/follow/isFollowing") suspend fun isFollowing(@Body body: FollowRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<Boolean>
    @POST("v1/follow") suspend fun toggleFollow(@Body body: FollowRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<Boolean>
    @POST("v1/follow/followers") suspend fun followers(@Body body: UsernameRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<Int>>
    @POST("v1/follow/following") suspend fun following(@Body body: UsernameRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<Int>>
    @POST("v1/like/isLike") suspend fun isLiked(@Body body: LikeRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<Boolean>>
    @POST("v1/like/quantityLikeByOne") suspend fun likeCount(@Body body: PostIdDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<Int>>
    @POST("v1/like/add") suspend fun like(@Body body: LikeRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<Any>>
    @POST("v1/like/delete") suspend fun unlike(@Body body: LikeRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<Any>>
    @POST("v1/users/updateInfo") suspend fun updateProfile(@Body body: UpdateProfileDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<UpdateProfileResponseDto>>
}
