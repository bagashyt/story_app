package com.bagashyt.myintermediate.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bagashyt.myintermediate.R
import com.bagashyt.myintermediate.adapter.ListStoryAdapter
import com.bagashyt.myintermediate.adapter.LoadingStateAdapter
import com.bagashyt.myintermediate.data.model.StoryModel
import com.bagashyt.myintermediate.databinding.ActivityMainBinding
import com.bagashyt.myintermediate.ui.add.AddStoryActivity
import com.bagashyt.myintermediate.ui.auth.AuthActivity
import com.bagashyt.myintermediate.ui.location.LocationActivity
import com.bagashyt.myintermediate.utils.animateVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@ExperimentalPagingApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var listAdapter: ListStoryAdapter

    private var token: String = ""
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        token = intent.getStringExtra(EXTRA_TOKEN)!!

        getAllStories()
        setRecyclerView()
        swipeRefreshStories()

        binding.fabStory.setOnClickListener {
            Intent(this, AddStoryActivity::class.java).also {
                startActivity(it)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                viewModel.deleteAuthToken()
                viewModel.saveAuthToken("")
                Intent(this, AuthActivity::class.java).also { intent ->
                    startActivity(intent)
                    finish()
                }
                true
            }
            R.id.menu_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.menu_location -> {
                Intent(this, LocationActivity::class.java).also {
                    startActivity(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getAllStories() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getListStories(token).collect { result ->

                    updateRecyclerViewData(result)
                    /*
                        result.onSuccess { response ->

                            binding.apply {
                                ivNotFound.animateVisibility(response.stories.isEmpty())
                                tvNotFound.animateVisibility(response.stories.isEmpty())
                                rvStories.animateVisibility(response.stories.isNotEmpty())
                                viewLoading.animateVisibility(false)
                                swipeRefresh.isRefreshing = false
                            }
                        }
                        result.onFailure {
                            showToast(this@MainActivity, getString(R.string.error_occured))

                            binding.apply {
                                tvNotFound.animateVisibility(true)
                                ivNotFound.animateVisibility(true)
                                rvStories.animateVisibility(false)
                                viewLoading.animateVisibility(false)
                                swipeRefresh.isRefreshing = false
                            }
                        }*/
                }
            }
        }
    }

    private fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        listAdapter = ListStoryAdapter()

        listAdapter.addLoadStateListener { loadState ->
            if ((loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && listAdapter.itemCount < 1) ||
                loadState.source.refresh is LoadState.Error
            ) {
                binding.apply {
                    tvNotFound.animateVisibility(true)
                    ivNotFound.animateVisibility(true)
                    rvStories.animateVisibility(false)
                }
            } else {
                binding.apply {
                    tvNotFound.animateVisibility(false)
                    ivNotFound.animateVisibility(false)
                    rvStories.animateVisibility(true)
                }
            }
            binding.swipeRefresh.isRefreshing = loadState.source.refresh is LoadState.Loading
        }
        try {
            recyclerView = binding.rvStories
            recyclerView.apply {
                adapter = listAdapter.withLoadStateFooter(
                    footer = LoadingStateAdapter {
                        listAdapter.retry()
                    }
                )
                layoutManager = linearLayoutManager
            }

        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun updateRecyclerViewData(stories: PagingData<StoryModel>) {
        val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()

        listAdapter.submitData(lifecycle, stories)
        recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun swipeRefreshStories() {
        binding.swipeRefresh.setOnRefreshListener {
            getAllStories()
            binding.viewLoading.animateVisibility(false)
        }
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }
}