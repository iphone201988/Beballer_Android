package com.beballer.beballer.ui.organizers.profile

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.LoginApiResponse
import com.beballer.beballer.data.model.OrganizerProfileData
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.FragmentOrganizersProfileBinding
import com.beballer.beballer.ui.organizers.dash_board.OrganizersDashBoardActivity
import com.beballer.beballer.ui.player.dash_board.DashboardActivity
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrganizersProfileFragment : BaseFragment<FragmentOrganizersProfileBinding>() {
    private val viewModel: OrganizersProfileFragmentVM by viewModels()


    override fun getLayoutResource(): Int {
        return R.layout.fragment_organizers_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        initObserver()
    }

    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner , Observer{
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "uniqueName" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){

                                val userData = OrganizerProfileData(
                                    username = binding.etCompanyName.text.toString().trim(),
                                    feedCountry =  binding.countryCodePicker.selectedCountryNameCode,
                                    email = binding.etEmailAddress.text.toString().trim()
                                )

                                val intent = Intent(requireContext(), UserProfileActivity::class.java)
                                intent.putExtra("userType", "add_organizer_pic")
                                intent.putExtra("userData", userData)
                                startActivity(intent)
                                requireActivity().overridePendingTransition(
                                    R.anim.slide_in_right, R.anim.slide_out_left
                                )
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
        })
    }


    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.btnNext -> {
                    if (validate()) {
                        val data = HashMap<String , Any>()
                        data["username"] = binding.etCompanyName.text.toString().trim()

                        viewModel.uniqueName(data, Constants.UNIQUE_NAME)
                    }
                }

            }
        }


        // Add TextWatchers
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsNotEmpty()
            }
        }

        binding.etCompanyName.addTextChangedListener(textWatcher)
        binding.etEmailAddress.addTextChangedListener(textWatcher)

    }

    // Function to check all fields
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty =
            binding.etCompanyName.text?.isNotEmpty() == true && binding.etEmailAddress.text?.isNotEmpty() == true
        binding.buttonCheck = isAllNotEmpty
    }

    /*** add validation ***/
    private fun validate(): Boolean {
        val companyName = binding.etCompanyName.text.toString().trim()
        val emailAddress = binding.etEmailAddress.text.toString().trim()
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

        if (companyName.isEmpty()) {
            showInfoToast("Please enter company name")
            return false
        } else if (emailAddress.isEmpty()) {
            showInfoToast("Please enter email address")
            return false
        } else if (!emailAddress.matches(emailRegex)) {
            showInfoToast("Please enter a valid email address")
            return false
        }
        return true
    }


}