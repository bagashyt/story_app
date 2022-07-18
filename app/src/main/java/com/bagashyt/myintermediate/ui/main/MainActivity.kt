package com.bagashyt.myintermediate.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bagashyt.myintermediate.R
import com.bagashyt.myintermediate.adapter.StoryAdapter
import com.bagashyt.myintermediate.data.remote.response.Story
import com.bagashyt.myintermediate.databinding.ActivityMainBinding
import com.bagashyt.myintermediate.ui.add.AddStoryActivity
import com.bagashyt.myintermediate.ui.auth.AuthActivity
import com.bagashyt.myintermediate.utils.animateVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var listAdapter: StoryAdapter

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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getAllStories() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getAllStories(token).collect { result ->
                    result.onSuccess { response ->
                        updateRecyclerViewData(response.stories)

                        binding.apply {
                            ivNotFound.animateVisibility(response.stories.isEmpty())
                            tvNotFound.animateVisibility(response.stories.isEmpty())
                            rvStories.animateVisibility(response.stories.isNotEmpty())
                            viewLoading.animateVisibility(false)
                            swipeRefresh.isRefreshing = false
                        }
                    }
                    result.onFailure {
                        Toast.makeText(
                            this@MainActivity,
                            "An Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()

                        binding.apply {
                            tvNotFound.animateVisibility(true)
                            ivNotFound.animateVisibility(true)
                            rvStories.animateVisibility(false)
                            viewLoading.animateVisibility(false)
                            swipeRefresh.isRefreshing = false
                        }
                    }
                }
            }
        }
    }

    private fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        listAdapter = StoryAdapter()
        recyclerView = binding.rvStories
        recyclerView.apply {
            adapter = listAdapter
            layoutManager = linearLayoutManager
        }
    }

    private fun updateRecyclerViewData(stories: List<Story>) {
        val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()

        listAdapter.submitList(stories)
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