package com.example.shopingapp.network

import com.example.shopingapp.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ================= AUTH =================

    @POST("/api/auth/login")
    fun login(
        @Body req: LoginRequest
    ): Call<LoginResponse>


    // 🔥 FIREBASE LOGIN (GOOGLE / PHONE)
    @POST("/api/auth/firebase")
    fun firebaseLogin(
        @Body req: FirebaseLoginRequest
    ): Call<LoginResponse>
    @POST("/api/auth/register")

    fun register(@Body req: RegisterRequest): Call<SimpleResponse>

    // ================= PRODUCTS (PUBLIC) =================

    @GET("/api/products")
    fun getAllProducts(): Call<List<Product>>

    @GET("/api/products/{id}")
    fun getProductDetail(
        @Path("id") id: Long
    ): Call<ProductDetail>


    // ================= FAVORITES (JWT) =================

    @POST("/api/favorites/{productId}/toggle")
    fun toggleFavorite(
        @Path("productId") productId: Long
    ): Call<FavoriteResponse>

    @GET("/api/favorites")
    fun getMyFavorites(): Call<List<Product>>


    // ================= CART (JWT) =================

    @POST("/api/cart")
    fun addToCart(
        @Body body: CartAddRequest
    ): Call<ResponseBody>

    @GET("/api/cart")
    fun getMyCart(): Call<List<CartItem>>

    @PUT("/api/cart/{cartItemId}")
    fun updateCartQuantity(
        @Path("cartItemId") cartItemId: Long,
        @Query("quantity") quantity: Int
    ): Call<ResponseBody>

    @DELETE("/api/cart/{cartItemId}")
    fun deleteCartItem(
        @Path("cartItemId") cartItemId: Long
    ): Call<ResponseBody>


    // ================= ORDERS (JWT) =================

    @POST("/api/orders")
    fun createOrder(
        @Body body: OrderRequest
    ): Call<Order>

    @GET("/api/orders")
    fun getMyOrders(): Call<List<Order>>

    @GET("/api/orders/{orderId}")
    fun getOrderDetail(
        @Path("orderId") orderId: Long
    ): Call<Order>
}
