package com.bagashyt.myintermediate.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bagashyt.myintermediate.data.model.StoryModel
import com.bagashyt.myintermediate.databinding.ItemStoryBinding
import com.bagashyt.myintermediate.ui.detail.DetailStoryActivity
import com.bagashyt.myintermediate.ui.detail.DetailStoryActivity.Companion.EXTRA_DETAIL
import com.bagashyt.myintermediate.utils.setImageFromUrl

class StoryAdapter : ListAdapter<StoryModel, StoryAdapter.ViewHolder>(DiffCallback) {
    class ViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, storyModel: StoryModel) {
            binding.apply {
                tvName.text = storyModel.name
                ivStory.setImageFromUrl(context, storyModel.photoUrl!!)

                root.setOnClickListener {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            root.context as Activity,
                            Pair(ivStory, "iv_story"),
                            Pair(tvName, "tv_name")
                        )
                    Intent(context, DetailStoryActivity::class.java).also { intent ->
                        intent.putExtra(EXTRA_DETAIL, storyModel)
                        context.startActivity(intent, optionsCompat.toBundle())

                    }
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(holder.itemView.context, story)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<StoryModel>() {
            override fun areItemsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}