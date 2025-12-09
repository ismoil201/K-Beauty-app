package com.example.shopingapp.network

import com.example.shopingapp.model.CartAddRequest
import com.example.shopingapp.model.CartItem
import com.example.shopingapp.model.Order
import com.example.shopingapp.model.OrderRequest
import com.example.shopingapp.model.Product
import com.example.shopingapp.model.RegisterRequest
import com.example.shopingapp.model.SimpleResponse
import com.example.shopingapp.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ------------------------- AUTH -------------------------
    @POST("api/auth/register")
    fun register(@Body body: RegisterRequest): Call<SimpleResponse>

    // ------------------------- PRODUCTS -------------------------
    @GET("api/products")
    fun getAllProducts(): Call<List<Product>>

    @GET("api/products/{id}")
    fun getProductById(@Path("id") id: Int): Call<Product>

    @POST("api/products")
    fun createProduct(@Body product: Product): Call<Product>

    @PUT("api/products/{id}")
    fun updateProduct(@Path("id") id: Int, @Body product: Product): Call<Product>

    @DELETE("api/products/{id}")
    fun deleteProduct(@Path("id") id: Int): Call<Void>

    // ------------------------- ORDERS -------------------------
    @POST("api/orders/create")
    fun createOrder(@Body body: OrderRequest): Call<Order>

    @GET("api/orders/{id}")
    fun getOrderById(@Path("id") id: Int): Call<Order>

    @PUT("api/orders/{id}/status")
    fun updateOrderStatus(@Path("id") id: Int, @Body status: String): Call<Order>

    @DELETE("api/orders/{id}")
    fun deleteOrder(@Path("id") id: Int): Call<Void>

    // ------------------------- CART -------------------------
    @POST("api/cart/add")
    fun addToCart(@Body body: CartAddRequest): Call<CartItem>

    @GET("api/cart/{userId}")
    fun getUserCart(@Path("userId") userId: Int): Call<List<CartItem>>

    @PUT("api/cart/{cartId}")
    fun updateCart(
        @Path("cartId") cartId: Int,
        @Query("quantity") qty: Int
    ): Call<CartItem>

    @DELETE("api/cart/{cartId}")
    fun deleteCartItem(@Path("cartId") cartId: Int): Call<Void>

    // ------------------------- USERS (ADMIN) -------------------------
    @GET("api/users")
    fun getAllUsers(): Call<List<User>>

    @GET("api/users/{id}")
    fun getUserById(@Path("id") id: Int): Call<User>

    @POST("api/users")
    fun createUser(@Body user: User): Call<User>

    @PUT("api/users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: User): Call<User>

    @DELETE("api/users/{id}")
    fun deleteUser(@Path("id") id: Int): Call<Void>

    @PUT("api/users/{id}/role")
    fun changeUserRole(
        @Path("id") id: Int,
        @Query("role") role: String
    ): Call<User>
}