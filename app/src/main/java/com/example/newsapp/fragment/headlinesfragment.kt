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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.Adapter.ArticleAdapter
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.ViewModel
import com.example.newsapp.databinding.FragmentHeadlinesBinding
import com.example.newsapp.util.Constant
import com.example.newsapp.util.Resource

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [headlinesfragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class headlinesfragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var viewModel: ViewModel
    lateinit var adapterArticle: ArticleAdapter
    lateinit var btnretry: Button
    lateinit var error: TextView
    lateinit var headlinesError: CardView
    lateinit var binding: FragmentHeadlinesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlinesBinding.bind(view)

        headlinesError = view.findViewById(R.id.itemError)

        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.error, null)

        btnretry = view.findViewById(R.id.btnRetry)
        error = view.findViewById(R.id.error)

        viewModel = (activity as MainActivity).viewModel
        setupHeadlinesRecycler()

        adapterArticle.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)

            }
            findNavController().navigate(R.id.action_headlinesfragment_to_article_fragment, bundle)

        }
        viewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideError()
                    response.data?.let { newsReponse ->
                        adapterArticle.differ.submitList(newsReponse.articles.toList())
                        val totalPages = newsReponse.totalResults / Constant.QUERY_PAGE_SIZE
                        isLastpage = viewModel.headlinesPage == totalPages
                        if (isLastpage) {
                            binding.recyclerViewHeadlines.setPadding(0, 0, 0, 0)

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
            viewModel.getHeadlines("us")
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_headlines, container, false)
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment headlinesfragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = headlinesfragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
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
        headlinesError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showError(message: String) {
        headlinesError.visibility = View.VISIBLE
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
            val isTotalMoreThanVisible = totalItemCount >= Constant.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isLastItem && isNotBegining && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getHeadlines("us")
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

    private fun setupHeadlinesRecycler() {
        adapterArticle = ArticleAdapter()
        binding.recyclerViewHeadlines.apply {
            adapter = adapterArticle
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@headlinesfragment.scrollListener)
        }
    }


}