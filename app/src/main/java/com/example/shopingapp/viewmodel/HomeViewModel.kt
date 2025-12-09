package com.example.shopingapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shopingapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    val products = MutableLiveData<List<Product>>()
    var loaded = false   // API faqat bir marta chaqiladi

    fun loadProducts() {
        if (loaded) return   // ❗ Takror chaqilmasin !!!

        RetrofitClient.instance.getAllProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    products.value = response.body() ?: emptyList()
                    loaded = true
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {

            }
        })
    }
}
