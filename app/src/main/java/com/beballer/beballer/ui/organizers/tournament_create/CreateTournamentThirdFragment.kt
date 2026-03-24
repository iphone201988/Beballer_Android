package com.beballer.beballer.ui.organizers.tournament_create

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.CreateTournamentApiResponse
import com.beballer.beballer.databinding.AddPhotoDialogItemBinding
import com.beballer.beballer.databinding.CreateTournamentPopupBinding
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.databinding.FragmentCreateTournamentThirdBinding
import com.beballer.beballer.databinding.VideoImagePickerDialogBoxBinding
import com.beballer.beballer.ui.player.create_profile.choose_avtar.ChooseAvatarFragment.Companion.sendMultipartImage
import com.beballer.beballer.utils.AppUtils
import com.beballer.beballer.utils.AppUtils.createImageFile
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.Status
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class CreateTournamentThirdFragment : BaseFragment<FragmentCreateTournamentThirdBinding>() {
    private val viewModel: CommonTournamentVM by activityViewModels()
    private var selectedItem = 0
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    private var photoFile2: File? = null
    private var photoURI: Uri? = null


    private lateinit var createTournamentPopup :  BaseCustomDialog<CreateTournamentPopupBinding>
    private var tournamentType : String ? = null
    private var multipartPart1: MultipartBody.Part? = null
    private var multipartPart2: MultipartBody.Part? = null
    private var multipartPart3: MultipartBody.Part? = null
    private var clickType = 0

    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_tournament_third
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnCLick()
        initPopup()

        initObserver()
         tournamentType = arguments?.getString("type")
    }




    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "CREATE_TOURNAMENT" -> {
                            try {
                                val myDataModel : CreateTournamentApiResponse ?= BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null){
                                    if (myDataModel.event != null){
                                        viewModel.tournamentData.eventId = myDataModel.event.id
                                        BindingUtils.navigateWithSlide(
                                            findNavController(),
                                            R.id.tournamentNine,
                                            null

                                        )
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("error", "commonLoginAPi: $e")
                            }finally {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {
                }
            }
        }
    }



    private fun initPopup() {
        createTournamentPopup = BaseCustomDialog(requireContext(), R.layout.create_tournament_popup){
            when(it.id){
                R.id.btnConfirm ->{
                    viewModel.createTournament()


                    createTournamentPopup.dismiss()
                }
                R.id.btnCancel ->{
                    createTournamentPopup.dismiss()
                }
            }

        }
    }

    /*** click handel ***/
    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    findNavController().popBackStack()
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

                R.id.btnNext -> {

                    viewModel.selectedImageParts.clear()

                    multipartPart1?.let { viewModel.selectedImageParts.add(it) }
                    multipartPart2?.let { viewModel.selectedImageParts.add(it) }
                    multipartPart3?.let { viewModel.selectedImageParts.add(it) }


                    if (tournamentType == "Multiple tournaments"){
                        createTournamentPopup.show()

                        tournamentType = ""
                    }
                    else{
                        BindingUtils.navigateWithSlide(
                            findNavController(),
                            R.id.tournamentFour,
                            null
                        )
                    }

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
                binding.cardAddImage1.visibility = View.VISIBLE
                binding.ivFabFirst.visibility = View.INVISIBLE
                binding.ivFirst.visibility = View.VISIBLE
                bindImage(uri, binding.ivFirst) { multipartPart1 = it }
            }

            2 -> {
                binding.cardAddImage2.visibility = View.VISIBLE
                binding.ivFabSecond.visibility = View.INVISIBLE
                binding.ivSecond.visibility = View.VISIBLE
                bindImage(uri, binding.ivSecond) { multipartPart2 = it }
            }

            3 -> {
                binding.cardAddImage3.visibility = View.VISIBLE
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
          imageView.setImageURI(uri)
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
                "eventPhotos",   // 🔥 FIXED KEY
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )

        } catch (e: Exception) {
            Log.d("error", "uriToMultipart:$e")
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



}