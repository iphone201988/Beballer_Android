package com.beballer.beballer.ui.player.dash_board.find.game.game_score

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.local.SharedPrefManager
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.data.model.GameDetail
import com.beballer.beballer.data.model.GameScoreDetails
import com.beballer.beballer.data.model.GameUserRole
import com.beballer.beballer.data.model.MatchResult
import com.beballer.beballer.data.model.ScoreboardState
import com.beballer.beballer.data.model.ValidationState
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel


class GameScoreVm @Inject constructor(private val sharedPrefManager: SharedPrefManager , private val apiHelper: ApiHelper) : BaseViewModel() {

    val commonObserver = SingleRequestEvent<JsonObject>()


    private val _gameScoreDetails = MutableLiveData<GameScoreDetails>()
    val gameScoreDetails: LiveData<GameScoreDetails> = _gameScoreDetails

    fun loadGame(game: GameDetail?) {
        mapGameScoreDetails(game)?.let {
            _gameScoreDetails.value = it
        }
    }

    private fun mapGameScoreDetails(game: GameDetail?): GameScoreDetails? {

        val status = game?.status ?: return null
        val gameEndDate = game.date ?: return null
        val creationDate = game.creationDate ?: return null
        val field = game.field ?: return null

        val endDate = BindingUtils.convertToISODate(gameEndDate) ?: return null
        val startDate = BindingUtils.convertToISODate(creationDate) ?: return null

        val profile = sharedPrefManager.getLoginData()?.data?.user ?: return null
        val loggedUserId = profile.id ?: return null
        val loggedUserInternalId = profile._id ?: return null

        val scoreTeam1 = game.scoreTeam1 ?: 0
        val scoreTeam2 = game.scoreTeam2 ?: 0
        val teamToValidate = game.teamToValidate ?: 0

        val team1ScoreTeam1 = game.team1ScoreTeam1 ?: 0
        val team1ScoreTeam2 = game.team1ScoreTeam2 ?: 0
        val team2ScoreTeam1 = game.team2ScoreTeam1 ?: 0
        val team2ScoreTeam2 = game.team2ScoreTeam2 ?: 0

        val dateString = BindingUtils.formattedMatchDate(endDate)
        val courtName = field.name ?: "Unknown field"
        val title = "Match on $dateString\n($courtName)"

        val role = resolveUserRole(
            loggedUserInternalId,
            game
        )

        /* ---------------------------------------------------- */
        /* FIX 1: Correct match finished logic                  */
        /* ---------------------------------------------------- */

        val isMatchFinished = status == "done" || (scoreTeam1 != 0 || scoreTeam2 != 0)
        val isMatchDone = status == "done"

        val isNoScoreProposed =
            team1ScoreTeam1 == 0 &&
                    team1ScoreTeam2 == 0 &&
                    team2ScoreTeam1 == 0 &&
                    team2ScoreTeam2 == 0

        /* ---------------------------------------------------- */
        /* Days Left Logic                                      */
        /* ---------------------------------------------------- */

        val maxValidationDays = 3
        val secondsInDay = 24 * 60 * 60

        val elapsedDays =
            ((System.currentTimeMillis() - startDate.time) / 1000 / secondsInDay).toInt()

        val daysLeft = maxOf(0, maxValidationDays - elapsedDays)

        /* ---------------------------------------------------- */
        /* Turn Logic                                           */
        /* ---------------------------------------------------- */

        val myTeamNumber: Int? = when (role) {
            GameUserRole.TEAM1 -> 1
            GameUserRole.TEAM2 -> 2
            GameUserRole.SPECTATOR -> null
        }

        val isMyTurn = myTeamNumber != null && myTeamNumber == teamToValidate

        val didMyTeamPropose = when (role) {
            GameUserRole.TEAM1 ->
                team1ScoreTeam1 != 0 || team1ScoreTeam2 != 0

            GameUserRole.TEAM2 ->
                team2ScoreTeam1 != 0 || team2ScoreTeam2 != 0

            GameUserRole.SPECTATOR ->
                false
        }

        val validationState = when {
            role == GameUserRole.SPECTATOR -> ValidationState.NOT_YOUR_TURN
            !isMyTurn -> ValidationState.NOT_YOUR_TURN
            didMyTeamPropose -> ValidationState.COUNTER_VALIDATION
            else -> ValidationState.FIRST_VALIDATION
        }

        val isWaitingForOpponent =
            role != GameUserRole.SPECTATOR && !isMyTurn

        /* ---------------------------------------------------- */
        /* Score Mapping                                        */
        /* ---------------------------------------------------- */

        val myScore = when (role) {
            GameUserRole.TEAM1 ->
                team1ScoreTeam1 to team1ScoreTeam2

            GameUserRole.TEAM2 ->
                team2ScoreTeam1 to team2ScoreTeam2

            GameUserRole.SPECTATOR ->
                scoreTeam1 to scoreTeam2
        }

        val opponentScore = when (role) {
            GameUserRole.TEAM1 ->
                team2ScoreTeam1 to team2ScoreTeam2

            GameUserRole.TEAM2 ->
                team1ScoreTeam1 to team1ScoreTeam2

            GameUserRole.SPECTATOR ->
                scoreTeam1 to scoreTeam2
        }

        /* ---------------------------------------------------- */
        /* Scoreboard State                                     */
        /* ---------------------------------------------------- */

        val scoreboardState = when {
            role == GameUserRole.SPECTATOR ->
                ScoreboardState.SPECTATOR

            isMatchFinished ->
                ScoreboardState.FINISHED

            isNoScoreProposed ->
                ScoreboardState.NO_SCORE_PROPOSED

            isMyTurn ->
                ScoreboardState.YOUR_TURN_TO_VALIDATE

            else ->
                ScoreboardState.WAITING_FOR_OPPONENT_VALIDATION
        }

        /* ---------------------------------------------------- */
        /* Message                                              */
        /* ---------------------------------------------------- */

        val message = makeScoreboardMessage(
            state = scoreboardState,
            role = role,
            scoreTeam1 = scoreTeam1,
            scoreTeam2 = scoreTeam2,
            opponentScore = opponentScore,
            daysLeft = daysLeft,
            validationState = validationState
        )

        return GameScoreDetails(
            scoreBoardTitle = title,
            scoreBoardMessage = message,
            role = role,
            scoreboardState = scoreboardState,
            validationState = if (role == GameUserRole.SPECTATOR) null else validationState,
            status = status,
            isMatchDone = isMatchDone,
            isMatchFinished = isMatchFinished,
            isNoScoreProposed = isNoScoreProposed,
            isMyTurn = isMyTurn,
            isWaitingForOpponent = isWaitingForOpponent,
            didMyTeamPropose = didMyTeamPropose,
            officialScore = scoreTeam1 to scoreTeam2,
            myScore = myScore,
            opponentScore = opponentScore,
            daysLeftForValidation = daysLeft
        )
    }

    /* ---------------------------------------------------- */
    /* Message Builder                                      */
    /* ---------------------------------------------------- */

    private fun makeScoreboardMessage(
        state: ScoreboardState,
        role: GameUserRole,
        scoreTeam1: Int,
        scoreTeam2: Int,
        opponentScore: Pair<Int, Int>,
        daysLeft: Int,
        validationState: ValidationState
    ): String {

        return when (state) {

            ScoreboardState.SPECTATOR ->
                "Match score"

            ScoreboardState.FINISHED -> {
                when (result(role, scoreTeam1, scoreTeam2)) {
                    MatchResult.WIN -> "Your team won 😃"
                     MatchResult.LOSS -> "Your team lost. You'll do better next time 💪"
                     MatchResult.DRAW -> "Draw."
                    null -> "Match finished."
                }
            }

            ScoreboardState.NO_SCORE_PROPOSED -> {
                if (daysLeft > 1) {
                    "You have $daysLeft days left to submit the final score. If not validated, your team will receive a 5-point penalty."
                } else {
                    "You have 1 day left to submit the final score. If not validated, your team will receive a 5-point penalty."
                }
            }

            ScoreboardState.YOUR_TURN_TO_VALIDATE -> {
                if (validationState == ValidationState.COUNTER_VALIDATION) {
                    "Your proposed score was rejected. Opponent proposes ${opponentScore.first} - ${opponentScore.second}."
                } else {
                    "Please validate the score proposed by the opposing team."
                }
            }

            ScoreboardState.WAITING_FOR_OPPONENT_VALIDATION ->
                "Waiting for the opposing team to validate the score."
        }
    }

    private fun result(
        role: GameUserRole,
        score1: Int,
        score2: Int
    ):  MatchResult? {

        if (role == GameUserRole.SPECTATOR) return null
        if (score1 == score2) return  MatchResult.DRAW

        return when (role) {
            GameUserRole.TEAM1 ->
                if (score1 > score2)  MatchResult.WIN else  MatchResult.LOSS

            GameUserRole.TEAM2 ->
                if (score2 > score1)  MatchResult.WIN else  MatchResult.LOSS

            GameUserRole.SPECTATOR ->
                null
        }
    }


    private fun resolveUserRole(
        loggedUserInternalId: String,
        game: GameDetail
    ): GameUserRole {

        // Check Team 1 (Organizer already included here)
        val isInTeam1 = game.team1Players
            ?.filterNotNull()
            ?.any { it._id == loggedUserInternalId }
            ?: false

        if (isInTeam1) return GameUserRole.TEAM1

        // Check Team 2
        val isInTeam2 = game.team2Players
            ?.filterNotNull()
            ?.any { it._id == loggedUserInternalId }
            ?: false

        if (isInTeam2) return GameUserRole.TEAM2

        // Otherwise spectator
        return GameUserRole.SPECTATOR
    }




    // api calls

    fun getGameDetails(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiGetWithoutQuery(url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("getGameDetails", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }


    fun confirmScore(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiPostForRawBody(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("confirmScore", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }

    fun updateScore(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiPostForRawBody(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("updateScore", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }

}