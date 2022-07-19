package com.bagashyt.myintermediate.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bagashyt.myintermediate.data.remote.response.Story
import com.bagashyt.myintermediate.databinding.ActivityDetailStoryBinding
import com.bagashyt.myintermediate.utils.setImageFromUrl

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<Story>(EXTRA_DETAIL)
        parseStoryData(story!!)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun parseStoryData(story: Story) {
        binding.apply {
            tvName.text = story.name
            tvDescription.text = story.description

            ivStory.setImageFromUrl(this@DetailStoryActivity, story.photoUrl)
        }
    }

    companion object {
        const val EXTRA_DETAIL = "extra_detail"
    }
}