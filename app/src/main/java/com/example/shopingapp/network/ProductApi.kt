package com.example.shopingapp.network

import com.example.shopingapp.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ================= AUTH =================
    @POST("api/auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @POST("api/auth/register")
    fun register(@Body body: RegisterRequest): Call<SimpleResponse>

    @POST("api/auth/firebase")
    fun firebaseLogin(@Body body: FirebaseLoginRequest): Call<LoginResponse>

    // ================= PRODUCTS (PUBLIC) =================
    @GET("api/products")
    fun getAllProducts(): Call<List<Product>>

    @GET("api/products/{id}")
    fun getProductDetail(@Path("id") id: Long): Call<ProductDetail>   // ✅ Long

    // ================= FAVORITES (JWT) =================
    @POST("api/favorites/{productId}/toggle")
    fun toggleFavorite(@Path("productId") productId: Long): Call<FavoriteResponse> // ✅ Long

    @GET("api/favorites")
    fun getMyFavorites(): Call<List<Product>>

    // ================= CART (JWT) =================
    @POST("api/cart")
    fun addToCart(@Body body: CartAddRequest): Call<SimpleResponse>

    @GET("api/cart")
    fun getMyCart(): Call<List<CartItem>>

    @PUT("api/cart/{cartItemId}")
    fun updateCartQuantity(
        @Path("cartItemId") cartItemId: Long,      // ✅ Long (tavsiya)
        @Query("quantity") quantity: Int
    ): Call<SimpleResponse>

    @DELETE("api/cart/{cartItemId}")
    fun deleteCartItem(@Path("cartItemId") cartItemId: Long): Call<SimpleResponse> // ✅ Long

    // ================= ORDERS (JWT) =================
    @POST("api/orders")
    fun createOrder(@Body body: OrderRequest): Call<Order>

    @GET("api/orders")
    fun getMyOrders(): Call<List<Order>>

    @GET("api/orders/{orderId}")
    fun getOrderDetail(@Path("orderId") orderId: Long): Call<Order>   // ✅ Long
}
