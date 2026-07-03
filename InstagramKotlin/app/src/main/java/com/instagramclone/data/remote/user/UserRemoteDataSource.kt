package com.instagramclone.data.remote.user

import com.instagramclone.core.network.ApiErrorMapper
import com.instagramclone.core.network.ApiResult
import com.instagramclone.core.network.SafeErrorContext
import com.instagramclone.core.session.CurrentUser
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val api: UserApi,
    private val errorMapper: ApiErrorMapper,
) {
    suspend fun getMyInfo(): ApiResult<CurrentUser> = try {
        when (val result = errorMapper.fromResponse(api.getMyInfo(), MY_INFO_CONTEXT)) {
            is ApiResult.Success -> result.value.toCurrentUser()
                ?.let { ApiResult.Success(it) }
                ?: ApiResult.DataError()
            is ApiResult.BusinessError -> result
            is ApiResult.ClientError -> result
            is ApiResult.DataError -> result
            is ApiResult.NetworkError -> result
            is ApiResult.ServerError -> result
            is ApiResult.Unauthorized -> result
            is ApiResult.UncategorizedError -> result
        }
    } catch (throwable: Throwable) {
        errorMapper.fromThrowable(throwable)
    }

    private companion object {
        val MY_INFO_CONTEXT = SafeErrorContext(method = "GET", path = "v1/users/my-info")
    }
}
