package com.example.newsapp

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsReponse
import com.example.newsapp.repository.Repository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException


class ViewModel(app: Application, val Repository: Repository) : AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsReponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesReponse: NewsReponse? = null

    val searchNews: MutableLiveData<Resource<NewsReponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsReponse? = null
    var newsSearchQuery: String? = null
    var oldSearchQuery: String? = null


    init {
        getHeadlines("us")
    }

    fun getHeadlines(countrycode: String) = viewModelScope.launch {
        headlinesInternet(countrycode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(reponse: Response<NewsReponse>): Resource<NewsReponse> {
        if (reponse.isSuccessful) {
            reponse.body()?.let { result ->
                headlinesPage++
                if (headlinesReponse == null) {
                    headlinesReponse = result
                } else {
                    val oldArticles = headlinesReponse?.articles
                    val newArticles = result.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesReponse ?: result)
            }
        }
        return Resource.Error(reponse.message())

    }

    private fun SearchNewsResponse(response: Response<NewsReponse>): Resource<NewsReponse> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                if (searchNewsResponse == null || newsSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newsSearchQuery
                    searchNewsResponse = result
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newsArticles = result.articles
                    oldArticles?.addAll(newsArticles)
                }
                return Resource.Success(searchNewsResponse ?: result)
            }
        }
        return Resource.Error(response.message())
    }

    fun insertToFavorite(article: Article) = viewModelScope.launch {
        Repository.upsert(article)
    }

    fun getFavorite() = Repository.getFavoriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        Repository.deleteArticle(article)
    }

    private fun connection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun headlinesInternet(countrycode: String) {
        headlines.postValue(Resource.Loading())

        try {
            if (connection(this.getApplication())) {
                val response = Repository.getHeadlines(countrycode, headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No internet"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
        newsSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (connection(this.getApplication())) {
                val response = Repository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(SearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }
}