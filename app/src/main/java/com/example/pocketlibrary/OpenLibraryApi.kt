package com.example.pocketlibrary

import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface OpenLibraryApi {
    @GET("search.json")
        suspend fun searchBooks(
        @Query("query") q: String,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,cover_i",
        @Query("sort") sort: String? = null,
        @Query("lang") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): OpenLibraryResponse
}

@JsonClass(generateAdapter = true)
data class OpenLibraryResponse(
    @Json(name = "num_found") val numFound: Int,
    val start: Int,
    val docs: List<Book>
)

@JsonClass(generateAdapter = true)
data class Book(
    val title: String?,
    @Json(name = "author_name") val authorNames: List<String>?,
    @Json(name = "first_publish_year") val firstPublishYear: Int?,
    @Json(name = "cover_i") val coverId: Int?
) {
    val coverImageUrl: String?
        get() = coverId?.let { "https://covers.openlibrary.org/b/id/${it}-M.jpg" }
}


