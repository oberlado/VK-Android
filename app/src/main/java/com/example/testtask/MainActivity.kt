package com.example.testtask

import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewModel: MainViewModel
    private var searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable = Runnable { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        supportActionBar?.hide()

        try {
            mainRecyclerView = findViewById(R.id.mainRecyclerView)
            bottomNavigationView = findViewById(R.id.bottomNavigationView)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val widthDp = displayMetrics.widthPixels / displayMetrics.density
            val spanCount = (widthDp / 300).toInt()

            // Установка менеджера компоновки для RecyclerView
            mainRecyclerView.layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                GridLayoutManager(this, 1)
            } else {
                GridLayoutManager(this, spanCount)
            }

            // Установка поведения для BottomNavigationView
            val layoutParams = bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.behavior = BottomNavigationBehavior<BottomNavigationView>()

            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu)

            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.button_prev -> {
                        // Обработка нажатия на "Previous"
                        viewModel.previousPage()
                        true
                    }
                    R.id.footer_layout -> {
                        // Обработка нажатия на "Footer"
                        showPageSelectionDialog()
                        true
                    }
                    R.id.button_next -> {
                        // Обработка нажатия на "Next"
                        viewModel.nextPage()
                        true
                    }
                    else -> false
                }
            }

            // Инициализация ViewModel
            viewModel = ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(MainViewModel::class.java)

            // Наблюдение за LiveData в ViewModel
            viewModel.productsData.observe(this, Observer { products ->
                // Обновление UI при изменении данных
                mainRecyclerView.adapter = ProductAdapter(products)
            })

            viewModel.searchResults.observe(this, Observer { products ->
                // Обновление UI при изменении данных
                mainRecyclerView.adapter = ProductAdapter(products)
            })

        } catch (e: Exception) {
            // Обработка ошибок
            Toast.makeText(this, "Произошла ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPageSelectionDialog() {
        if (viewModel.totalPages > 0 && viewModel.totalPages < Int.MAX_VALUE) {
            val pages = Array(viewModel.totalPages) { "Страница ${it + 1}" }
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Выберите страницу")

            val adapter = object : ArrayAdapter<String>(this, android.R.layout.select_dialog_item, pages) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    // Если текущая страница, то выделяем жирным и делаем некликабельной
                    if (position == viewModel.currentPage) {
                        textView.setTypeface(textView.typeface, Typeface.BOLD)
                        textView.isEnabled = false
                    } else {
                        textView.setTypeface(null, Typeface.NORMAL)
                        textView.isEnabled = true
                    }

                    return view
                }
            }

            builder.setAdapter(adapter) { dialog, which ->
                // Переход на выбранную страницу
                navigateToPage(pages[which])
            }

            builder.show()
        } else {
            Toast.makeText(this, "Страницы не найдены или их слишком много", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToPage(page: String) {
        val pageNumber = page.removePrefix("Страница ").toInt()
        if (pageNumber in 1..viewModel.totalPages) {
            viewModel.currentPage = pageNumber - 1
        } else {
            Toast.makeText(this, "Неверный номер страницы: $page", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as android.widget.SearchView

        // Запрос фокуса и открытие клавиатуры
        searchView.isFocusable = true
        searchView.isIconified = false
        searchView.requestFocusFromTouch()

        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Выполните поиск здесь и/или отправьте запрос на бэкенд
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Вы можете использовать этот метод для реализации поиска "по мере ввода"
                return false
            }
        })

        return true
    }

    private fun performSearch(query: String) {
        // Очистите текущие результаты поиска
        viewModel.clearSearchResults()

        // Выполните локальный поиск
        viewModel.performLocalSearch(query)

        // Выполните поиск на бэкенде
        viewModel.performBackendSearch("https://dummyjson.com/products/search&q=$query")
    }
}

















