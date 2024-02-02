package com.example.newsapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.model.Article

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    lateinit var imgArticle: ImageView
    lateinit var articleSource: TextView
    lateinit var articletitle: TextView
    lateinit var articleDescription: TextView
    lateinit var articleDatetime: TextView

    private val differCallBack = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList.get(position)
        if (article == null){
            return;
        }
        imgArticle = holder.itemView.findViewById(R.id.Image)
        articleSource = holder.itemView.findViewById(R.id.source)
        articletitle = holder.itemView.findViewById(R.id.title)
        articleDescription = holder.itemView.findViewById(R.id.description)
        articleDatetime = holder.itemView.findViewById(R.id.datetime)

        holder.itemView.apply {
            Glide.with(this).load(article.urlToImage).into(imgArticle)
            articleSource.text = article.source?.name ?: "Unknown Source"
            articletitle.text = article.title
            articleDescription.text = article.description
            articleDatetime.text = article.publishedAt


            setOnClickListener {
                onItemClickListener?.let {
                    it(article)
                }
            }

        }

    }

    private var onItemClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }
}