package com.beballer.beballer.ui.player.dash_board.find.game.find_game

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.data.model.FindModel
import com.beballer.beballer.data.model.GetCourtApiResponse
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.GetPlayersApiResponse
import com.beballer.beballer.databinding.FindGameRvItemBinding
import com.beballer.beballer.databinding.FragmentFindGameBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.CourtAdapter
import com.beballer.beballer.ui.player.dash_board.find.courts.ViewItem
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindGameFragment : BaseFragment<FragmentFindGameBinding>() {
    private val viewModel: FindGameFragmentVM by viewModels()
    private var fullList = ArrayList<GetCourtData?>()

    private var query : String ? = null
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var searchHandler: Handler? = null
    private var searchRunnable: Runnable? = null
    private var isProgress = false

    private lateinit var findGameAdapter: FindGameAdapter


    override fun getLayoutResource(): Int {
        return R.layout.fragment_find_game
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()

        initObserver()
        getCourts()
        // adapter
        initFindAdapter()
        setupSearch()
        // pagination
        pagination()
        /** Refresh **/
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.organize_color))
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                // api call
                getCourts()
            }, 2000)
        }

    }

    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING ->  {
                    if (!isProgress) {
                        showLoading()
                    }
                }
                Status.SUCCESS ->  {
                    hideLoading()
                    when(it.message){
                        "getCourts" ->{
                            try {
                                val myDataModel: GetCourtApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.courts?.isNotEmpty() == true) {
                                        val pastSessionData = myDataModel.courts
                                        val feedItems: List<ViewItem> =
                                            pastSessionData.filterNotNull()
                                                .map { ViewItem.Post(it) } ?: emptyList()
                                        isLoading = false
                                        isLastPage = false
                                        isProgress = true

                                        if (currentPage == 1) {
                                            myDataModel.courts.let {
                                                fullList = it as ArrayList<GetCourtData?>
                                                findGameAdapter.setList(feedItems)

                                            }
                                            Log.i("fdsfsd", "initObserver: $fullList")
                                        } else {
                                            findGameAdapter.addToList(feedItems)
                                        }
                                        isLastPage =
                                            currentPage == myDataModel.pagination?.totalPages
                                    }

                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                findGameAdapter.hideLoader()
                                hideLoading()
                            }
                        }
                    }
                }
                Status.ERROR ->  {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }

    private fun getCourts() {
        val params = HashMap<String, Any>()
        params["page"] = 1
        params["limit"] = 10
        binding.courtsSearchView.clearFocus()
        viewModel.getCourts(Constants.GET_COURTS, params)
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when(it?.id){
                R.id.cancelImage->{
                    findNavController().popBackStack()
                }

                R.id.tvAddCourt->{
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "addCourt")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }
        }


    }


    /** handle adapter **/
    private fun initFindAdapter() {
        findGameAdapter = FindGameAdapter(object : FindGameAdapter.OnItemClickListener {
            override fun onItemClick(item: GetCourtData?, clickedViewId: Int, position: Int) {
                when (clickedViewId) {
                    R.id.clMain -> {
//                        val intent = Intent(requireContext(), UserProfileActivity::class.java)
//                        intent.putExtra("userType", "createGame")
//                        intent.putExtra("courtData", item)
//                        startActivity(intent)
//                        requireActivity().overridePendingTransition(
//                            R.anim.slide_in_right, R.anim.slide_out_left
//                        )

                        if (item != null) {

                            val resultIntent = Intent().apply {
                                putExtra("courtData", item)
                            }

                            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                            requireActivity().finish()

                        } else {
                            showErrorToast("Please select court ")
                        }
                    }

                }
            }
        })
        binding.rvFindGame.adapter = findGameAdapter

    }






    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvFindGame.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }

        })
    }

    /**
     *  load more function call
     **/
    private fun loadMoreItems() {
        findGameAdapter.showLoader()
        isProgress = true
        isLoading = true
        currentPage++
        val params = HashMap<String, Any>()
        params["page"] = currentPage
        params["limit"] = 10
        binding.courtsSearchView.clearFocus()
        viewModel.getCourts(Constants.GET_COURTS, params)
    }


    /*** add search ***/
    private fun setupSearch() {
        val searchView = binding.courtsSearchView
        searchHandler = Handler(requireActivity().mainLooper)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (trimmedQuery.isNullOrEmpty() || trimmedQuery.isBlank()) {
                    getCourts()
                } else {
                    currentPage = 1
                    searchCourts(trimmedQuery)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler?.removeCallbacks(it) }
                searchRunnable = Runnable {
                    if (newText != null) {
                        val trimmedQuery = newText.trim()
                        if (trimmedQuery.isNotEmpty() && trimmedQuery.isNotBlank()) {
                            currentPage = 1
                            searchCourts(trimmedQuery)
                        } else if (newText.isEmpty()) {
                            currentPage = 1
                            getCourts()
                        }
                    }
                }
                searchHandler?.postDelayed(searchRunnable!!, 1000)
                return true
            }
        })
    }

    /**
     * search court
     */
    private fun searchCourts(query: String) {
        val params = HashMap<String, Any>()
        params["page"] = 1
        params["limit"] = 50
        params["search"] = query
        binding.courtsSearchView.clearFocus()
        viewModel.getCourts(Constants.GET_COURTS, params)
    }





}