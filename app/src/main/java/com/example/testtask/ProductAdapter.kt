package com.example.testtask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(private val products: List<Product>?) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView? = view.findViewById(R.id.title)
        val description: TextView? = view.findViewById(R.id.description)
        val thumbnail: ImageView? = view.findViewById(R.id.thumbnail)
        val price: TextView? = view.findViewById(R.id.price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products?.get(position)
        if (product != null) {
            holder.title?.text = product.title
            holder.description?.text = product.description
            if (holder.thumbnail != null) {
                Glide.with(holder.thumbnail.context).load(product.thumbnail).into(holder.thumbnail)
            }
            holder.price?.text = product.price.toString() + "$"
        }
    }

    override fun getItemCount() = products?.size ?: 0
}


