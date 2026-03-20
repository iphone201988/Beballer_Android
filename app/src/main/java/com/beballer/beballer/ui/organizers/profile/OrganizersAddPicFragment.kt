package com.beballer.beballer.ui.organizers.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.LoginApiResponse
import com.beballer.beballer.data.model.OrganizerProfileData
import com.beballer.beballer.databinding.FragmentOrganizersAddPicBinding
import com.beballer.beballer.databinding.VideoImagePickerDialogBoxBinding
import com.beballer.beballer.ui.organizers.dash_board.OrganizersDashBoardActivity
import com.beballer.beballer.ui.player.dash_board.DashboardActivity
import com.beballer.beballer.utils.AppUtils
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import kotlin.getValue
import kotlin.io.extension


@AndroidEntryPoint
class OrganizersAddPicFragment : BaseFragment<FragmentOrganizersAddPicBinding>() {

    private val viewModel: OrganizersProfileFragmentVM by viewModels()
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    private var multipartImage: MultipartBody.Part? = null

    private var organizerData : OrganizerProfileData ? = null
    private var photoFile2: File? = null
    private var photoURI: Uri? = null
    private var uri: Uri? = null

    private var token = ""



    override fun getLayoutResource(): Int {
        return R.layout.fragment_organizers_add_pic
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initData()
        initOnClick()


        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            token = it.result

        }
        initObserver()
    }

    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    showLoading()
                }
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "createOrganizer" ->{
                            try {
                                val myDataModel: LoginApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    myDataModel.data?.token?.let { it1 ->
                                        sharedPrefManager.saveToken(
                                            it1
                                        )
                                    }
                                    sharedPrefManager.setLoginData(myDataModel)
                                    val intent = Intent(requireContext(),OrganizersDashBoardActivity::class.java)
                                        startActivity(intent)
                                        requireActivity().finishAffinity()
                                    } else {
                                        BindingUtils.navigateWithSlide(
                                            findNavController(), R.id.navigateOptionFragment, null
                                        )
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
                else -> {}

            }
        })
    }

    private fun initData() {
        organizerData =  arguments?.getParcelable("userData")
        Log.i("dsfsdfs", "initData: $organizerData")
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner , Observer{
            when(it?.id){
                R.id.ivAddPic , R.id.ivProfileImage->{
                    imageDialog()
                }
                R.id.btnNext ->{
                    val data  = HashMap<String , RequestBody>()
                    data["feedCountry"] = organizerData?.feedCountry.toString().toRequestBody()
                    data["latitude"] = BindingUtils.lat.toString().toRequestBody()
                    data["email"] = organizerData?.email.toString().toRequestBody()
                    data["deviceToken"] = token.toRequestBody()
                    data["deviceType"] = "2".toRequestBody()
                    data["username"] =  organizerData?.username.toString().toRequestBody()
                    data["type"] = "organizer".toRequestBody()
                    data["longitude"] = BindingUtils.long.toString().toRequestBody()


                    if (multipartImage!=null){
                        viewModel.createOrganizer(data, multipartImage)
                    }else{
                        viewModel.createOrganizer(data, null)
                    }
                }
            }
        })
    }


    /**** Edit date and time dialog  handel ***/
    private fun imageDialog() {
        imageDialog = BaseCustomDialog(requireContext(), R.layout.video_image_picker_dialog_box) {
            when (it.id) {
                R.id.tvCamera, R.id.imageCamera -> {
                    if (!BindingUtils.hasPermissions(
                            requireContext(), BindingUtils.permissions
                        )
                    ) {
                        permissionResultLauncher1.launch(BindingUtils.permissions)
                    } else {
                        // camera
                        openCameraIntent()
                    }
                    imageDialog!!.dismiss()
                }

                R.id.imageGallery, R.id.tvGallery -> {
                    if (!BindingUtils.hasPermissions(
                            requireContext(), BindingUtils.permissions
                        )
                    ) {
                        permissionResultLauncher.launch(BindingUtils.permissions)

                    } else {
                        galleryImagePicker()

                    }
                    imageDialog!!.dismiss()
                }

            }
        }
        imageDialog!!.create()
        imageDialog!!.show()

    }


    /**** Gallery permission  ***/
    private var allGranted = false
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (it in permissions.entries) {
                it.key
                val isGranted = it.value
                allGranted = isGranted
            }
            when {
                allGranted -> {
                    galleryImagePicker()
                }

                else -> {
                    showInfoToast("Permission Denied")
                }
            }
        }

    /*** open gallery ***/
    private fun galleryImagePicker() {
        val pictureActionIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI).apply {
                type = "image/*"  // Restrict to images only
            }
        resultLauncherGallery.launch(pictureActionIntent)
    }

    /*** gallery launcher ***/
    private var resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                imageUri?.let {
                    uri =it
                    binding.buttonCheck = true
                    multipartImage = convertMultipartPartGal(it)
                    binding.ivProfileImage.setImageURI(imageUri)

                }
            }
        }


    private val permissionResultLauncher1: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                openCameraIntent()
            } else {
                showInfoToast("Permission Denied")
            }

        }

    /**** open camera ***/
    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(requireContext().packageManager) != null) {
            try {
                photoFile2 = AppUtils.createImageFile1(requireContext())
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            if (photoFile2 != null) {
                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "com.beballer.beballer.fileProvider",
                    photoFile2!!
                )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                resultLauncherCamera.launch(pictureIntent)
            }else{
                Log.d("TAG", "openCameraIntent: ")
            }
        }
        else{
            Log.d("TAG", "openCameraIntent: ")
        }
    }


    /*** camera launcher ***/
    private val resultLauncherCamera: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (photoFile2?.exists() == true) {
                    val imagePath = photoFile2?.absolutePath.toString()
                    val imageUri = imagePath.toUri()
                    imageUri.let {
                        uri =it
                        binding.buttonCheck = true
                        multipartImage = convertMultipartPart(it)
                        binding.ivProfileImage.setImageURI(imageUri)

                    }
                }
            }
        }


    private fun convertMultipartPartGal(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireActivity(), imageUri)
        val fileName =
            "${file!!.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
        val newFile = File(file.parent, fileName)
        file.renameTo(newFile)
        return MultipartBody.Part.createFormData(
            "profilePicture", newFile.name, newFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }

    private fun convertMultipartPart(imageUri: Uri): MultipartBody.Part? {
        val filePath = imageUri.path ?: return null
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("profilePicture", file.name, requestFile)
    }



}