package com.beballer.beballer.ui.image_zoom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.databinding.FragmentImageZoomBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.Objects


class ImageZoomFragment : BaseFragment<FragmentImageZoomBinding>() {


    private val viewModel : ImageZoomViewModel by viewModels()

    private var url : String ?= null


    override fun getLayoutResource(): Int {
        return R.layout.fragment_image_zoom
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        initView()


    }
    private fun initView() {
        url= arguments?.getString("url").toString()
        Log.i("url", "initView: $url")


            if (!url.isNullOrBlank()) {

                val imageUrl = if (url!!.startsWith("/")) {
                    Constants.IMAGE_URL + url
                } else {
                    Constants.IMAGE_URL + "/$url"
                }

                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.progress_animation_small)
                    .error(R.drawable.ic_round_account_circle_40)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.iamge)
            }

    }
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.ivCross ->{}

            }
        })
    }

}