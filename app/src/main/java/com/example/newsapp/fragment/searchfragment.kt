package com.example.newsapp.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.Adapter.ArticleAdapter
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.ViewModel
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.util.Constant
import com.example.newsapp.util.Constant.Companion.TIME_DELAY
import com.example.newsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [searchfragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class searchfragment : Fragment() {

    lateinit var viewModel: ViewModel
    lateinit var AdapterArticle: ArticleAdapter
    lateinit var btnretry: Button
    lateinit var error: TextView
    lateinit var searchError: CardView
    lateinit var binding: FragmentSearchBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        searchError = view.findViewById(R.id.searchError)

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.error, null)

        btnretry = view.findViewById(R.id.btnRetry)
        error = view.findViewById(R.id.error)

        viewModel = (activity as MainActivity).viewModel
        setupRecyclerView()

        AdapterArticle.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)

            }
            findNavController().navigate(R.id.action_searchfragment_to_article_fragment, bundle)

        }
        var job: Job? = null
        binding.searchEdt.addTextChangedListener() { edt ->
            job?.cancel()
            job = MainScope().launch {
                delay(TIME_DELAY)
                edt?.let {
                    if (edt.toString().isNotEmpty()) {
                        viewModel.searchNews(edt.toString())
                    }

                }
            }
        }
        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideError()
                    response.data?.let { newsReponse ->
                        AdapterArticle.differ.submitList(newsReponse.articles.toList())
                        val totalPages = newsReponse.totalResults / Constant.QUERY_PAGE_SIZE
                        isLastpage = viewModel.searchNewsPage == totalPages
                        if (isLastpage) {
                            binding.recyclerViewSearch.setPadding(0, 0, 0, 0)

                        }
                    }
                }
                is Resource.Loading<*> -> {
                    showProgressBar()
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "error: $message", Toast.LENGTH_LONG)
                        showError(message)
                    }
                }
            }
        })
        btnretry.setOnClickListener {
            if (binding.searchEdt.text.toString().isNotEmpty()){
                viewModel.searchNews(binding.searchEdt.text.toString())
            } else{
                hideError()
            }
        }

    }

    var isError = false
    var isLoading = false
    var isLastpage = false
    var isScrolling = false

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideError() {
        searchError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showError(message: String) {
        searchError.visibility = View.VISIBLE
        error.text = message
        isError = true
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastpage
            val isLastItem = firstItemPosition + visibleItemCount >= totalItemCount
            val isNotBegining = firstItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constant.QUERY_PAGE_SIZE + 2

            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isLastItem && isNotBegining && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.searchNews(binding.searchEdt.text.toString())
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

    }

    private fun setupRecyclerView() {
        AdapterArticle = ArticleAdapter()
        binding.recyclerViewSearch.apply {
            adapter = AdapterArticle
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@searchfragment.scrollListener)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View = inflater.inflate(R.layout.fragment_search, container, false)
        return view
    }


}