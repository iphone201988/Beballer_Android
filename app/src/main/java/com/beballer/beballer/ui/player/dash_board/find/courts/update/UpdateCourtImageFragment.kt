package com.beballer.beballer.ui.player.dash_board.find.courts.update

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.CourtDataById
import com.beballer.beballer.data.model.UpdateCourtData
import com.beballer.beballer.databinding.AddPhotoDialogItemBinding
import com.beballer.beballer.databinding.FragmentUpdateCourtImageBinding
import com.beballer.beballer.databinding.VideoImagePickerDialogBoxBinding
import com.beballer.beballer.ui.enums.ImageType
import com.beballer.beballer.utils.AppUtils.createImageFile
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class UpdateCourtImageFragment : BaseFragment<FragmentUpdateCourtImageBinding>() {

    private val viewModel: UpdateCourtImageFragmentVM by viewModels()
    private var addDialogItem: BaseCustomDialog<AddPhotoDialogItemBinding>? = null
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    private var multipartPart1: MultipartBody.Part? = null
    private var multipartPart2: MultipartBody.Part? = null
    private var multipartPart3: MultipartBody.Part? = null
    private var clickType: ImageType = ImageType.FIRST

    private var courtId: String? = null

    private var grade = 0.0f

    override fun getLayoutResource(): Int {
        return R.layout.fragment_update_court_image
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnclick()

        arguments?.let { bundle ->
            // prefill if editing
            bundle.getParcelable<CourtDataById>("courtData")?.let { courtData ->
                binding.etCourtDescription.setText(courtData.description)

                binding.courtRatingBar.rating = courtData.rating?.toFloat() ?: 0.0f

                grade = courtData.rating?.toFloat() ?: 0.0f

                binding.tvRatings.text = grade.toString()

                courtId = courtData.id

                courtData.photos?.forEachIndexed { index, url ->
                    val imageUrl = if (url.startsWith("/")) {
                        Constants.IMAGE_URL + url
                    } else {
                        Constants.IMAGE_URL + "/$url"
                    }

                    when (index) {
                        0 -> {
                            binding.cardAddImage1.visibility = View.INVISIBLE
                            binding.ivFabFirst.visibility = View.INVISIBLE
                            binding.ivFirst.visibility = View.VISIBLE
                            BindingUtils.setImageFromUrl(binding.ivFirst, url)

                            lifecycleScope.launch {
                                multipartPart1 = withContext(Dispatchers.IO) {
                                    urlToMultipartWithGlide(imageUrl)
                                }
                            }
                        }

                        1 -> {
                            binding.cardAddImage2.visibility = View.INVISIBLE
                            binding.ivFabSecond.visibility = View.INVISIBLE
                            binding.ivSecond.visibility = View.VISIBLE
                            BindingUtils.setImageFromUrl(binding.ivSecond, url)

                            lifecycleScope.launch {
                                multipartPart2 = withContext(Dispatchers.IO) {
                                    urlToMultipartWithGlide(imageUrl)
                                }
                            }
                        }

                        2 -> {
                            binding.cardAddImage3.visibility = View.INVISIBLE
                            binding.ivFabThree.visibility = View.INVISIBLE
                            binding.ivThird.visibility = View.VISIBLE
                            BindingUtils.setImageFromUrl(binding.ivThird, url)

                            lifecycleScope.launch {
                                multipartPart3 = withContext(Dispatchers.IO) {
                                    urlToMultipartWithGlide(imageUrl)
                                }
                            }
                        }
                    }
                }

            }
        }

        // observer
        initObserver()


        binding.courtRatingBar.setOnRatingBarChangeListener = { rating, fromUser ->
            if (fromUser) {
                grade = rating
                binding.tvRatings.text = rating.toString()
            }
        }


    }

    /*** click event handel **/
    private fun initOnclick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    requireActivity().finish()
                }

                R.id.btnNext -> {
                    val images = getImageParts()
                    val desc = binding.etCourtDescription.text.toString().trim()

                    // If not editing, at least one photo might be required
                    if (courtId.isNullOrEmpty() && images.isEmpty()) {
                        showInfoToast("Please select at least one photo")
                        return@observe
                    }

                    if (desc.isEmpty()) {
                        showInfoToast("Please enter description")
                        return@observe
                    }

                    viewModel.updateImageCourt(
                        grade = grade.toString(), desc, images.toMutableList(), courtId
                    )


                }

                R.id.ivFabFirst, R.id.ivFirst -> {
                    clickType = ImageType.FIRST
                    imageDialog()
                }

                R.id.ivFabThree, R.id.ivThird -> {
                    clickType = ImageType.THIRD
                    imageDialog()
                }

                R.id.ivFabSecond, R.id.ivSecond -> {
                    clickType = ImageType.SECOND
                    imageDialog()
                }

            }
        }

    }


    private fun getImageParts(): List<MultipartBody.Part> {
        val list = mutableListOf<MultipartBody.Part>()

        multipartPart1?.let { list.add(it) }
        multipartPart2?.let { list.add(it) }
        multipartPart3?.let { list.add(it) }

        return list
    }

    /**
     * Method to initialize observer
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    when (it.message) {
                        "updateAPiCall" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<UpdateCourtData>(it.data.toString())
                                if (model?.success == true) {
                                    showSuccessToast(model.message)
                                    showAddPhotoDialogItem()
                                } else {
                                    showErrorToast(model?.message.toString())
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                Status.LOADING -> showLoading()
                else -> {

                }
            }
        }
    }


    /**** image pick dialog  handel ***/
    private fun imageDialog() {
        imageDialog = BaseCustomDialog<VideoImagePickerDialogBoxBinding>(
            requireActivity(), R.layout.video_image_picker_dialog_box
        ) { v ->
            when (v.id) {
                R.id.tvCamera, R.id.imageCamera -> {
                    if (!hasCameraPermission(requireContext())) {
                        permissionResultLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCamera()
                    }
                    imageDialog?.dismiss()
                }

                R.id.imageGallery, R.id.tvGallery -> {
                    openGallery()
                    imageDialog?.dismiss()
                }
            }
        }
        imageDialog!!.create()
        imageDialog!!.show()

    }


    /**
     * Method to open gallery
     */
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * Method to handle selected image
     */
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleSelectedImage(it) }
        }

    /**
     * Method to open camera
     */
    private fun openCamera() {
        photoFile = createImageFile(requireContext())
        photoURI = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileProvider", photoFile!!
        )

        cameraLauncher.launch(photoURI)
    }

    /**
     * Method to handle selected image
     */
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoURI != null) {
                handleSelectedImage(photoURI!!)
                photoURI = null
            }
        }

    /**
     * Method to handle selected image
     */
    private fun handleSelectedImage(uri: Uri) {
        when (clickType) {
            ImageType.FIRST -> {
                multipartPart1 = null
                binding.cardAddImage1.visibility = View.INVISIBLE
                binding.ivFabFirst.visibility = View.INVISIBLE
                binding.ivFirst.visibility = View.VISIBLE
                bindImage(uri, binding.ivFirst) { multipartPart1 = it }
            }

            ImageType.SECOND -> {
                multipartPart2 = null
                binding.cardAddImage2.visibility = View.INVISIBLE
                binding.ivFabSecond.visibility = View.INVISIBLE
                binding.ivSecond.visibility = View.VISIBLE
                bindImage(uri, binding.ivSecond) { multipartPart2 = it }
            }

            ImageType.THIRD -> {
                multipartPart3 = null
                binding.cardAddImage3.visibility = View.INVISIBLE
                binding.ivFabThree.visibility = View.INVISIBLE
                binding.ivThird.visibility = View.VISIBLE
                bindImage(uri, binding.ivThird) { multipartPart3 = it }
            }

        }
    }

    /**
     * Method to bind image
     */
    private fun bindImage(
        uri: Uri, imageView: ImageView, onMultipartReady: (MultipartBody.Part?) -> Unit
    ) {

        Glide.with(this).load(uri).placeholder(R.drawable.progress_animation_small)
            .error(R.drawable.ic_round_account_circle_40).diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
        onMultipartReady(uriToMultipart(uri))
    }

    /**
     * Method to convert uri to multipart
     */
    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null

            val file = File(requireContext().cacheDir, "img_${System.currentTimeMillis()}.jpg")

            file.outputStream().use { output ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
            }
            MultipartBody.Part.createFormData(
                "photos", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } catch (e: Exception) {
            Log.d("error", "uriToMultipart:$e: ")
            null
        }
    }

    /**
     * Method to check camera permission
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Method to check camera permission
     */

    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                openCamera()
            } else {
                showInfoToast("Permission Denied")
            }
        }


    /**** alert dialog item ****/
    private fun showAddPhotoDialogItem() {
        addDialogItem = BaseCustomDialog<AddPhotoDialogItemBinding>(
            requireContext(), R.layout.add_photo_dialog_item
        ) { v ->
            when (v.id) {
                R.id.tvBtn -> {
                    addDialogItem?.dismiss()
                    requireActivity().finish()
                }
            }
        }
        addDialogItem?.create()
        addDialogItem?.show()
    }

    /**
     * Method to convert url to multipart
     */
    private suspend fun urlToMultipartWithGlide(url: String): MultipartBody.Part? {
        return try {
            val futureTarget = Glide.with(requireContext()).asBitmap().load(url).submit()

            val bitmap = withContext(Dispatchers.IO) {
                futureTarget.get()
            }

            val file = File(requireContext().cacheDir, "img_${System.currentTimeMillis()}.jpg")

            file.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
            }

            Glide.with(requireContext()).clear(futureTarget)

            MultipartBody.Part.createFormData(
                "photos", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
            )

        } catch (e: Exception) {
            Log.d("GlideMultipart", "Error: ${e.message}")
            null
        }
    }

}




