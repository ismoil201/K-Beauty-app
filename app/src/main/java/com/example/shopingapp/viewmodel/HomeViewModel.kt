package com.example.shopingapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.shopingapp.model.Product
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(application: Application) :
    AndroidViewModel(application) {

    val products = MutableLiveData<List<Product>>()
    private var loaded = false

    private val api = RetrofitClient.instance(application)

    fun loadProducts() {
        if (loaded) return

        api.getAllProducts()
            .enqueue(object : Callback<List<Product>> {

                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                    Log.d("HOME_VM", "code=${response.code()}")
                    Log.d("HOME_VM", "body=${response.body()}")

                    if (response.isSuccessful) {
                        products.value = response.body() ?: emptyList()
                        loaded = true
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Log.e("HOME_VM", "API error", t)
                }
            })
    }
}
