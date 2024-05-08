package com.example.testtask

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.ceil

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _productsData = MutableLiveData<List<Product>>()
    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> get() = _searchResults
    val productsData: LiveData<List<Product>> get() = _productsData

    private var totalProducts = 0
    private var productsOnPage = 20
    var totalPages = 0

    private val sharedPreferences = application.getSharedPreferences("MyApp", Context.MODE_PRIVATE)

    var currentPage: Int
        get() = sharedPreferences.getInt("currentPage", 0)
        set(value) {
            sharedPreferences.edit().putInt("currentPage", value).apply()
            try {
                loadProducts("https://dummyjson.com/products?skip=${value * productsOnPage}&limit=${productsOnPage}")
            } catch (_: Exception) {
            }
        }

    init {
        try {
            loadTotalProducts("https://dummyjson.com/products")
        } catch (_: Exception) {
        }
    }
    init {
        currentPage = 0
    }

    fun loadTotalProducts(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val json = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val totalProducts = JSONObject(json).getInt("total")

                withContext(Dispatchers.Main) {
                    this@MainViewModel.totalProducts = totalProducts
                    totalPages = ceil(totalProducts.toDouble() / productsOnPage).toInt()
                    try {
                        loadProducts("https://dummyjson.com/products?skip=0&limit=${productsOnPage}")
                    } catch (_: Exception) {
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                }
            }
        }
    }

    fun loadProducts(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val json = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val products = JSONObject(json).getJSONArray("products")
                val result = mutableListOf<Product>()
                for (i in 0 until products.length()) {
                    val product = products.getJSONObject(i)
                    val title = product.getString("title")
                    val description = product.getString("description")
                    val thumbnail = product.getString("thumbnail")
                    val price = product.getDouble("price")
                    result.add(Product(title, description, thumbnail, price))
                }

                withContext(Dispatchers.Main) {
                    if (result.isEmpty()) {
                        currentPage = currentPage.minus(1)
                    } else {
                        _productsData.value = result
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                }
            }
        }
    }

    fun nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage = currentPage.plus(1)
            try {
                loadProducts("https://dummyjson.com/products?skip=${currentPage * productsOnPage}&limit=${productsOnPage}")
            } catch (_: Exception) {
            }
        }
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage = currentPage.minus(1)
            try {
                loadProducts("https://dummyjson.com/products?skip=${currentPage * productsOnPage}&limit=${productsOnPage}")
            } catch (_: Exception) {
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun performLocalSearch(query: String) {
        // Фильтрация списка товаров на основе запроса
        val results = _productsData.value?.filter { product ->
            product.title.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
        } ?: emptyList()
        // Установка результатов в _searchResults
        _searchResults.value = results
    }


    fun performBackendSearch(s: String) {

    }
}




