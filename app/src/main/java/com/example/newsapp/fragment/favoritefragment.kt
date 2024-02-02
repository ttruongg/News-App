package com.example.newsapp.fragment

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.Adapter.ArticleAdapter
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.ViewModel
import com.example.newsapp.databinding.FragmentFavoriteBinding
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [favoritefragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class favoritefragment : Fragment() {
    lateinit var viewModel: ViewModel
    lateinit var adapterArticle: ArticleAdapter
    lateinit var binding: FragmentFavoriteBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoriteBinding.bind(view)

        viewModel = (activity as MainActivity).viewModel
        setupRecyclerFavorite()

        adapterArticle.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_favoritefragment_to_article_fragment, bundle)
        }
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val postion = viewHolder.adapterPosition
                val article = adapterArticle.differ.currentList.get(postion)
                viewModel.deleteArticle(article)
                Snackbar.make(view, " Delete Successful", Snackbar.LENGTH_LONG).apply {
                    setAction("Back") {
                        viewModel.insertToFavorite(article)
                    }
                    show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                var icon: Drawable?
                var background: ColorDrawable
                val itemView = viewHolder.itemView
                val backgroundCornerOffset = 20


                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                background = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.red))


                assert(icon != null)
                val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight

                if (dX > 0) { //  right
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
                    )
                } else if (dX < 0) { //  left
                    val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                } else { //  unSwiped
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)
                icon.draw(c)
            }
        }

        ItemTouchHelper(itemTouchHelper).apply {
            attachToRecyclerView(binding.recyclerViewFavorite)
        }

        viewModel.getFavorite().observe(viewLifecycleOwner, Observer { article ->
            adapterArticle.differ.submitList(article)
        })
    }

    private fun setupRecyclerFavorite() {
        adapterArticle = ArticleAdapter()
        binding.recyclerViewFavorite.apply {
            adapter = adapterArticle
            layoutManager = LinearLayoutManager(activity)

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }


}