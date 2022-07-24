package com.bagashyt.myintermediate.ui.add

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.bagashyt.myintermediate.R
import com.bagashyt.myintermediate.databinding.ActivityAddStoryBinding
import com.bagashyt.myintermediate.utils.*
import com.bagashyt.myintermediate.utils.MediaUtil.reduceFileImage
import com.bagashyt.myintermediate.utils.MediaUtil.uriToFile
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@AndroidEntryPoint
@ExperimentalPagingApi
class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentPhotoPath: String
    private lateinit var fusetLocationClient: FusedLocationProviderClient

    private var getFile: File? = null
    private var location: Location? = null
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

        fusetLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getCurrentLocation()
            } else {
                this.location = null
            }
        }
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
            lifecycleScope.launchWhenStarted {
                launch {

                    val file = reduceFileImage(getFile as File)
                    val description =
                        edtDescription.text.toString().toRequestBody("text/plain".toMediaType())
                    val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "photo",
                        file.name,
                        requestImageFile
                    )
                    var lat: RequestBody? = null
                    var lon: RequestBody? = null

                    if (location != null) {
                        lat =
                            location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                        lon =
                            location?.longitude.toString().toRequestBody("text/plain".toMediaType())
                    }

                    viewModel.uploadImage(token, imageMultipart, description, lat, lon)
                        .collect { response ->
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

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusetLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this.location = location
                    Log.d("TAG_Location", "LAT: ${location.latitude}, LON: ${location.longitude}")
                } else {
                    showToast(this, "Please check your GPS")
                    binding.switchLocation.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perimission ->
        when {
            perimission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getCurrentLocation()
            }
            else -> {
                Snackbar
                    .make(
                        binding.root,
                        "Location Permission not allowed",
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction("Change Setting") {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    .show()

                binding.switchLocation.isChecked = false
            }
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