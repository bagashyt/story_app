package com.bagashyt.myintermediate.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bagashyt.myintermediate.R
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

fun View.animateVisibility(isVisible: Boolean, duration: Long = 400L) {
    ObjectAnimator
        .ofFloat(this, View.ALPHA, if (isVisible) 1f else 0f)
        .setDuration(duration)
        .start()
}

fun ImageView.setImageFromUrl(context: Context, url: String) {
    Glide
        .with(context)
        .load(url)
        .placeholder(R.drawable.ic_placeholder_image)
        .error(R.drawable.ic_broken_image)
        .into(this)
}

fun showSnackbar(view: View, message: String){
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}

fun showToast(context: Context, msg: String){
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}