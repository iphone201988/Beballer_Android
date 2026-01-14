package com.beballer.beballer.ui.player.dash_board.find.game.add_photo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.AddCourtDataClass
import com.beballer.beballer.databinding.AddPhotoDialogItemBinding
import com.beballer.beballer.databinding.FragmentAddPhotoBinding
import com.beballer.beballer.databinding.VideoImagePickerDialogBoxBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.AppUtils.createImageFile
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class AddPhotoFragment : BaseFragment<FragmentAddPhotoBinding>() {
    private val viewModel: AddPhotoFragmentVM by viewModels()
    private lateinit var addDialogItem: BaseCustomDialog<AddPhotoDialogItemBinding>
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    private var photoFile2: File? = null
    private var photoURI: Uri? = null
    private var multipartPart1: MultipartBody.Part? = null
    private var multipartPart2: MultipartBody.Part? = null
    private var multipartPart3: MultipartBody.Part? = null
    private var clickType = 0

    private lateinit var courtName: String
    private lateinit var courtAddress: String
    private lateinit var accessibility: String
    private lateinit var hoopsCount: String
    private lateinit var lat: String
    private lateinit var long: String

    private lateinit var board: String
    private lateinit var net: String
    private lateinit var floor: String
    private lateinit var lines: String
    private lateinit var water: String
    private lateinit var city: String
    private lateinit var country: String
    private lateinit var region: String
    private lateinit var zipCode: String
    private var grade = 0.0f
    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_photo
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnclick()

        arguments?.let { bundle ->
            courtName = bundle.getString("courtName").orEmpty()
            courtAddress = bundle.getString("courtAddress").orEmpty()
            accessibility = bundle.getString("accessibility").orEmpty()
            hoopsCount = bundle.getString("hoopsCount").orEmpty()
            lat = bundle.getString("lat").orEmpty()
            long = bundle.getString("long").orEmpty()
            city = bundle.getString("city").orEmpty()
            country = bundle.getString("country").orEmpty()
            region = bundle.getString("region").orEmpty()
            zipCode = bundle.getString("zipCode").orEmpty()
            board = bundle.getString("board").orEmpty()
            net = bundle.getString("net").orEmpty()
            floor = bundle.getString("floor").orEmpty()
            lines = bundle.getString("lines").orEmpty()
            water = bundle.getString("water").orEmpty()

        }

        addPhotoDialogItem()

        // observer
        initObserver()


        binding.courtRatingBar.setOnRatingBarChangeListener = { rating, fromUser ->
            if (fromUser){
                grade = rating
            }
        }
    }

    /*** click event handel **/
    private fun initOnclick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    findNavController().popBackStack()
                }

                R.id.btnNext -> {
                    val images = getImageParts()
                    val desc = binding.etCourtDescription.text.toString().trim()

                    if (images.isEmpty()) {
                        showInfoToast("Please select at least one photo")
                        return@observe
                    }

                    if (desc.isEmpty()) {
                        showInfoToast("Please enter description")
                        return@observe
                    }
                    var hasWaterPoint = false
                    var dimensionsStandard = false
                    hasWaterPoint = water != "Without"
                    dimensionsStandard = lines != "Not up to standard"

                    viewModel.createCourt(
                        courtName,
                        courtAddress,
                        mapAccessibility(accessibility),
                        mapHoopsCount(hoopsCount),
                        lat,
                        long,
                        mapBoard(board),
                        mapNet(net),
                        mapFloor(floor),
                        mapWaterPoint(water),
                        mapDimensions(lines),
                        country,
                        grade = grade.toString(),
                        city,
                        zipCode,
                        region,
                        desc,
                        images.toMutableList()
                    )


                }

                R.id.ivFabFirst, R.id.ivFirst -> {
                    clickType = 1
                    imageDialog()
                }

                R.id.ivFabThree, R.id.ivThird -> {
                    clickType = 3
                    imageDialog()
                }

                R.id.ivFabSecond, R.id.ivSecond -> {
                    clickType = 2
                    imageDialog()
                }

            }
        }

    }


    private fun mapAccessibility(v: String) = when (v) {
        "Available to everyone" -> "availableToEveryone"
        "Available to licensees" -> "availableToLicensees"
        "Special opening hours" -> "specialOpeningHours"
        else -> "availableToEveryone"
    }

    private fun mapHoopsCount(v: String) = when (v) {
        "1" -> 1
        "2" -> 2
        "3" -> 3
        "4" -> 4
        "5+" -> 5
        else -> 1
    }

    private fun mapBoard(v: String) = when (v) {
        "Steel" -> "steel"
        "Wood" -> "wood"
        "Plastic" -> "plastic"
        "Plexiglas" -> "plexiglas"
        else -> "steel"
    }

    private fun mapNet(v: String) = when (v) {
        "String" -> "string"
        "Chain" -> "chains"
        "Plastic" -> "plastic"
        "No nets" -> "without"
        else -> "without"
    }

    private fun mapFloor(v: String) = when (v) {
        "Asphalt with gravel" -> "gravelBitumen"
        "Bitumen without gravel" -> "bitumen"
        "synth" -> "synthetic"
        "parquet" -> "woodenFloor"
        else -> "bitumen"
    }

    fun mapDimensions(value: String): Boolean {
        return value == "Up to standards"
    }


    fun mapWaterPoint(value: String): Boolean {
        return value == "With"
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
                        "createCourt" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<AddCourtDataClass>(it.data.toString())
                                if (model?.success == true && model.courtId?.isNotEmpty() == true) {
                                    showSuccessToast(model.message.toString())
                                    addPhotoDialogItem()
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
        imageDialog = BaseCustomDialog(requireActivity(), R.layout.video_image_picker_dialog_box) {
            when (it.id) {
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
        photoFile2 = createImageFile(requireContext())
        photoURI = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileProvider", photoFile2!!
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
            1 -> {
                binding.cardAddImage1.visibility = View.INVISIBLE
                binding.ivFabFirst.visibility = View.INVISIBLE
                binding.ivFirst.visibility = View.VISIBLE
                bindImage(uri, binding.ivFirst) { multipartPart1 = it }
            }

            2 -> {
                binding.cardAddImage2.visibility = View.INVISIBLE
                binding.ivFabSecond.visibility = View.INVISIBLE
                binding.ivSecond.visibility = View.VISIBLE
                bindImage(uri, binding.ivSecond) { multipartPart2 = it }
            }

            3 -> {
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
        Glide.with(this).load(uri).into(imageView)
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
                inputStream.copyTo(output)
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
    private fun addPhotoDialogItem() {
        addDialogItem = BaseCustomDialog<AddPhotoDialogItemBinding>(
            requireContext(), R.layout.add_photo_dialog_item
        ) {
            when (it?.id) {
                R.id.tvBtn -> {
                    addDialogItem.dismiss()
                    requireActivity().finish()
                }
            }

        }
        addDialogItem.create()
        addDialogItem.show()
    }


}