package com.example.newsapp.model

data class NewsReponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)