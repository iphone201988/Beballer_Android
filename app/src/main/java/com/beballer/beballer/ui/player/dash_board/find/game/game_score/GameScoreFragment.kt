package com.beballer.beballer.ui.player.dash_board.find.game.game_score

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.GameScoreDetails
import com.beballer.beballer.data.model.GetGameDetailsApiResponse
import com.beballer.beballer.data.model.ScoreboardState
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.FragmentGameScoreBinding
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GameScoreFragment : BaseFragment<FragmentGameScoreBinding>() {


    private var gameId : String ? = null
    private val viewModel  : GameScoreVm  by viewModels()

    override fun getLayoutResource(): Int {

        return R.layout.fragment_game_score
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {

        gameData()
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.gameScoreDetails.observe(viewLifecycleOwner) {
            populateGameDetails(it)
        }
         initOnClick()

        initObserver()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner){
            when(it?.id){
                R.id.cancelImage ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.btnConfirmThisScore ->{
                    val data = HashMap<String, Any>()
                    data["gameId"] = gameId.toString()

                    viewModel.confirmScore(data, Constants.VALIDATE_GAME)

                }
                R.id.btnPurposeThisScore ->{
                    val data = HashMap<String, Any>()
                    data["team1Score"] = binding.etHomeScore.text.toString().trim()
                    data["team2Score"] = binding.etAwayScore.text.toString().trim()
                    data["gameId"] = gameId.toString()

                    viewModel.updateScore(data, Constants.UPDATE_GAME_SCORE)
                }
                R.id.btnPurposeAnotherScore ->{
                    val data = HashMap<String, Any>()
                    data["team1Score"] = binding.etHomeScore.text.toString().trim()
                    data["team2Score"] = binding.etAwayScore.text.toString().trim()
                    data["gameId"] = gameId.toString()

                    viewModel.updateScore(data, Constants.UPDATE_GAME_SCORE)
                }
            }
        }
    }

    private fun initObserver() {

            viewModel.commonObserver.observe(viewLifecycleOwner, Observer {
                when (it?.status) {
                    Status.LOADING -> {
                        showLoading()
                    }
                    Status.SUCCESS -> {
                        hideLoading()

                        when (it.message) {
                            "getGameDetails" -> {
                                val myDataModel: GetGameDetailsApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())

                                val game = myDataModel?.game
                                viewModel.loadGame(game)

                            }

                            "confirmScore" -> {
                                val myDataModel: SimpleApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    showSuccessToast(myDataModel.message.toString())
                                    gameData()
                                }
                            }

                            "updateScore" -> {
                                val myDataModel: SimpleApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    showSuccessToast(myDataModel.message.toString())
                                    gameData()
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


    private fun gameData() {
        gameId = arguments?.getString("gameId")

        if (gameId != null){
            viewModel.getGameDetails(Constants.GET_GAME_BY_ID + gameId)

        }
    }


    private fun populateGameDetails(details: GameScoreDetails) {


        Log.i("dfdsfss", "populateGameDetails: $details")

        with(binding) {

            /* ------------------------------------ */
            /* Static Labels                        */
            /* ------------------------------------ */

            binding.date.text = details.scoreBoardTitle
            binding.gameStatus.text = details.scoreBoardMessage



            when (details.scoreboardState) {

                ScoreboardState.WAITING_FOR_OPPONENT_VALIDATION -> {
                    // Show my proposed score
                    etHomeScore.setText(details.myScore.first.toString())
                    etAwayScore.setText(details.myScore.second.toString())
                }

                ScoreboardState.YOUR_TURN_TO_VALIDATE -> {
                    // Show opponent proposed score
                    etHomeScore.setText(details.opponentScore.first.toString())
                    etAwayScore.setText(details.opponentScore.second.toString())
                }

                else -> {
                    // Default (optional)
                    etHomeScore.setText(details.myScore.first.toString())
                    etAwayScore.setText(details.myScore.second.toString())
                }
            }


            /* ------------------------------------ */
            /* Reset Everything First               */
            /* ------------------------------------ */

            etHomeScore.setText("")
            etAwayScore.setText("")

            etHomeScore.hint = "0"
            etAwayScore.hint = "0"

            etHomeScore.isEnabled = false
            etAwayScore.isEnabled = false

            btnPurposeThisScore.visibility = View.GONE
            btnConfirmThisScore.visibility = View.GONE
            btnPurposeAnotherScore.visibility = View.GONE


            /* ------------------------------------ */
            /* State Handling                       */
            /* ------------------------------------ */

            when (details.scoreboardState) {

                /* ----------------------------- */
                /* Spectator                    */
                /* ----------------------------- */
                ScoreboardState.SPECTATOR -> {

                    etHomeScore.setText(details.officialScore.first.toString())
                    etAwayScore.setText(details.officialScore.second.toString())

                    etHomeScore.isEnabled = false
                    etAwayScore.isEnabled = false
                }

                /* ----------------------------- */
                /* Finished                     */
                /* ----------------------------- */
                ScoreboardState.FINISHED -> {

                    etHomeScore.setText(details.officialScore.first.toString())
                    etAwayScore.setText(details.officialScore.second.toString())

                    etHomeScore.isEnabled = false
                    etAwayScore.isEnabled = false
                }

                /* ----------------------------- */
                /* No Score Proposed             */
                /* ----------------------------- */
                ScoreboardState.NO_SCORE_PROPOSED -> {

                    etHomeScore.isEnabled = true
                    etAwayScore.isEnabled = true

                    btnPurposeThisScore.visibility = View.VISIBLE
                }

                /* ----------------------------- */
                /* Waiting For Opponent          */
                /* ----------------------------- */
                ScoreboardState.WAITING_FOR_OPPONENT_VALIDATION -> {

                    etHomeScore.setText(details.myScore.first.toString())
                    etAwayScore.setText(details.myScore.second.toString())

                    etHomeScore.isEnabled = false
                    etAwayScore.isEnabled = false
                }

                /* ----------------------------- */
                /* Your Turn To Validate         */
                /* ----------------------------- */
                ScoreboardState.YOUR_TURN_TO_VALIDATE -> {

                    etHomeScore.setText(details.opponentScore.first.toString())
                    etAwayScore.setText(details.opponentScore.second.toString())

                    etHomeScore.isEnabled = true
                    etAwayScore.isEnabled = true

                    btnConfirmThisScore.visibility = View.VISIBLE
                    btnPurposeAnotherScore.visibility = View.VISIBLE
                }
            }
        }
    }

}