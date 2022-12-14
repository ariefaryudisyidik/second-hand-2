package com.binar.secondhand.data.source.remote.network

import com.binar.secondhand.data.source.remote.response.CategoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface SellerCategoryService {

    @GET("seller/category")
    suspend fun getCategories(): Response<List<CategoryResponse>>
}