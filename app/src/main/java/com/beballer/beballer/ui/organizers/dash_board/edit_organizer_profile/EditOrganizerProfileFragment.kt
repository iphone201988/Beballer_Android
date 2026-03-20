package com.beballer.beballer.ui.organizers.dash_board.edit_organizer_profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.OrganizerProfileData
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.data.model.UserProfile
import com.beballer.beballer.databinding.FragmentEditOrganizerProfileBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class EditOrganizerProfileFragment : BaseFragment<FragmentEditOrganizerProfileBinding>() {

    private val viewModel: EditOrganizerVm by viewModels()
    private var oldUsername: String = ""

    override fun getLayoutResource(): Int {
        return R.layout.fragment_edit_organizer_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        setupSystemUI()
        initObserver()
    }


    private fun setupSystemUI() {
        BindingUtils.applySystemBarMargins(binding.consMain)
        BindingUtils.statusBarStyleWhite(requireActivity())

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.tvChangePhoto -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "editImage")
                    intent.putExtra("side", "organizer")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
                R.id.tvSave -> {
                    oldUsername = binding.etUserName.text.toString().trim()

                    Log.i("fsdfsdf", "initOnClick: $oldUsername")
                    val newUsername = binding.etUserName.text.toString().trim()

                    if (newUsername.isEmpty()) {
                        binding.etUserName.error = "Username required"

                    } else if (newUsername == oldUsername) {

                        val data = HashMap<String, RequestBody>()

                        data["feedCountry"] =
                            binding.countryCode.selectedCountryName.toRequestBody("text/plain".toMediaTypeOrNull())

                        data["username"] =
                            newUsername.toRequestBody("text/plain".toMediaTypeOrNull())

                        data["longitude"] =
                            BindingUtils.long.toString().toRequestBody()

                        data["latitude"] =
                            BindingUtils.lat.toString().toRequestBody()

                        data["type"] =
                            "organizer".toRequestBody()

                        data["deviceType"] =
                            "2".toRequestBody()

                        data["profileDescription"] =
                            binding.tvDescription.text.toString().toRequestBody()

                        viewModel.updateProfileApi(Constants.EDIT_ORGANIZER, data, null)

                    } else {
                        val data = HashMap<String, Any>()
                        data["username"] = newUsername

                        viewModel.uniqueName(data, Constants.UNIQUE_NAME)
                    }
                }
            }
        })
    }


    /**  api response observer  **/
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getProfileApi" -> {
                            try {
                                val myDataModel: UserProfile? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        sharedPrefManager.setProfileData(myDataModel)
                                        binding.bean = myDataModel.data.user
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("error", "getProfileApi: $e")
                            } finally {

                                binding.btnDeleteAccount.visibility = View.VISIBLE
                                hideLoading()
                            }
                        }
                        "updateProfileApi" -> {
                            try {
                                val myDataModel: UserProfile? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    showSuccessToast(myDataModel.message.toString())
                                }

                            } catch (e: Exception) {
                                Log.e("error", "updateProfileApi: $e")
                            } finally {
                                hideLoading()
                            }
                        }

                        "uniqueName" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){

                                val userName = binding.etUserName.text.toString().trim()
                                val data = HashMap<String, RequestBody>()
                                data["feedCountry"] =
                                    binding.countryCode.selectedCountryName.toRequestBody("text/plain".toMediaTypeOrNull())
                                data["username"] = userName.toRequestBody("text/plain".toMediaTypeOrNull())
                                data["longitude"] = BindingUtils.long.toString().toRequestBody()
                                data["latitude"] = BindingUtils.lat.toString().toRequestBody()
                                data["type"] = "organizer".toRequestBody()
                                data["deviceType"] = 2.toString().toRequestBody()
                                data["profileDescription"] = binding.tvDescription.toString().toRequestBody()

                                viewModel.updateProfileApi(Constants.EDIT_ORGANIZER, data, null)
                            }
                        }

                    }
                }

                Status.ERROR -> {
                    hideLoading()

                    binding.btnDeleteAccount.visibility = View.GONE
                    showErrorToast(it.message.toString())
                }

                else -> {
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        // api call
        val data = HashMap<String, Any>()
        viewModel.getProfileApi(Constants.USER_PROFILE, data)
    }

}