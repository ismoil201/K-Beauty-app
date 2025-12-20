package com.example.shopingapp.data.network

import com.example.shopingapp.model.FirebaseLoginRequest
import com.example.shopingapp.model.LoginResponse
import com.example.shopingapp.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackendRepository(
    private val api: ApiService
) {

    fun firebaseLogin(
        token: String,
        onSuccess: (LoginResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.firebaseLogin(FirebaseLoginRequest(token))
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onError("Backend error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    onError(t.message ?: "Network error")
                }
            })
    }
}
