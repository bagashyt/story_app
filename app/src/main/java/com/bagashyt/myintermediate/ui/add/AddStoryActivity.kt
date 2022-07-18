package com.bagashyt.myintermediate.ui.add

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bagashyt.myintermediate.R
import com.bagashyt.myintermediate.databinding.ActivityAddStoryBinding
import com.bagashyt.myintermediate.utils.*
import com.bagashyt.myintermediate.utils.MediaUtil.reduceFileImage
import com.bagashyt.myintermediate.utils.MediaUtil.uriToFile
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentPhotoPath: String

    private var getFile: File? = null
    private var token: String = ""

    private val viewModel: AddStoryViewModel by viewModels()

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath).also { getFile = it }
            val os: OutputStream

            val bitmap = BitmapFactory.decodeFile(getFile?.path)
            val exif = ExifInterface(currentPhotoPath)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val rotateBitmap: Bitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
                ExifInterface.ORIENTATION_NORMAL -> bitmap
                else -> bitmap
            }

            try {
                os = FileOutputStream(file)
                rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()

                getFile = file
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.imageView.setImageBitmap(rotateBitmap)

        }
    }

    private val launchIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            uriToFile(selectedImg, this).also { getFile = it }

            binding.imageView.setImageFromUrl(this, selectedImg.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        lifecycleScope.launchWhenCreated {
            launch {
                viewModel.getAuthToken().collect { authToken ->
                    if (!authToken.isNullOrEmpty()) token = authToken
                }
            }
        }

        binding.btnCamera.setOnClickListener { startIntentCamera() }
        binding.btnGallery.setOnClickListener { startIntentGallery() }
        binding.btnUpload.setOnClickListener { uploadStory() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun uploadStory() {
        setLoadingState(true)

        val edtDescription = binding.edtDescription
        var isValid = true

        if (edtDescription.text.toString().isBlank()) {
            edtDescription.error = getString(R.string.fill_description)
            isValid = false
        }

        if (getFile == null) {
            showSnackbar(binding.root, getString(R.string.select_image))
            isValid = false
        }

        if (isValid) {
            val file = reduceFileImage(getFile as File)
            val description =
                edtDescription.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            lifecycleScope.launchWhenStarted {
                launch {
                    viewModel.uploadImage(token, imageMultipart, description).collect { response ->
                        response.onSuccess {
                            showToast(this@AddStoryActivity, getString(R.string.story_uploaded))
                            finish()
                        }

                        response.onFailure {
                            setLoadingState(false)
                            showSnackbar(binding.root, getString(R.string.failed_upload))
                        }
                    }
                }
            }
        } else setLoadingState(false)
    }


    private fun startIntentGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launchIntentGallery.launch(chooser)
    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun startIntentCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
        intent.resolveActivity(packageManager)

        MediaUtil.createTempFile(application).also {
            val photoUri = FileProvider.getUriForFile(
                this,
                "com.bagashyt.myintermediate",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.apply {
            btnCamera.isEnabled = !isLoading
            btnGallery.isEnabled = !isLoading
            edtDescription.isEnabled = !isLoading

            viewLoading.animateVisibility(isLoading)
        }
    }

}