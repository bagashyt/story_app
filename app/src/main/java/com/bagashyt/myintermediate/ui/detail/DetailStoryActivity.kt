package com.bagashyt.myintermediate.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bagashyt.myintermediate.data.model.StoryModel
import com.bagashyt.myintermediate.databinding.ActivityDetailStoryBinding
import com.bagashyt.myintermediate.utils.setImageFromUrl

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyModel = intent.getParcelableExtra<StoryModel>(EXTRA_DETAIL)
        parseStoryData(storyModel!!)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun parseStoryData(storyModel: StoryModel) {
        binding.apply {
            tvName.text = storyModel.name
            tvDescription.text = storyModel.description

            ivStory.setImageFromUrl(this@DetailStoryActivity, storyModel.photoUrl!!)
        }
    }

    companion object {
        const val EXTRA_DETAIL = "extra_detail"
    }
}