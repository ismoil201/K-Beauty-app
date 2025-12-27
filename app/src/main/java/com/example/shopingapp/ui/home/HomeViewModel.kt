package com.example.shopingapp.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.shopingapp.model.PageResponse
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
            .enqueue(object : Callback<PageResponse<Product>> {

                override fun onResponse(
                    call: Call<PageResponse<Product>>,
                    response: Response<PageResponse<Product>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        products.value = response.body()!!.content
                        loaded = true
                    }
                }

                override fun onFailure(call: Call<PageResponse<Product>>, t: Throwable) {
                    Log.e("HOME_VM", "API error", t)
                }
            })

    }
}
