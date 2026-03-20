package com.beballer.beballer.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.android.parcel.Parcelize


data class OptionModel( var emoji: String ,var title: String, var positionId:Int,var positionCode: String,var check: Boolean =false)
data class AvatarModel( var avatar: Int,var id:Int,var check: Boolean =false)
data class PlayerPostModel( var avatar: Int,var title:String,var type: Int)
data class FindModel( var image: Int,var title:String,var id:Int,var check: Boolean =false)
data class MpvModel( var name: String ,var desc: String, var point: String )
data class RankingModel( var code: String , var image :Int ,var name: String, var userName: String)
data class StatistModel( var name: String , var point :Int)
data class InventModel( var match: String , var percentage :Int)
data class GameModeModel( var title: String )
data class GameModes( var title: String ,  val modeId: Int =  0)
data class SubscriptionModel( var title: String )
data class ChatModel( var message: String,var chatType: Boolean=false)
data class SettingsModel( var teamIcon: Int,var type:String , var colorCode:String)
data class CreateTournamentModel( var name: String)
data class AddTournamentModel( var name: String)
data class FinalModel( var name: String,var check: Boolean=false)
data class MatchModel( var headingTitle: String,var title: String,var subTitle: String , var newTeam :String , var team:String)
data class PoolModel( var count: String,var teamName: String,var point: String)


data class MapBounds(
    val northEastLat: Double,
    val northEastLng: Double,
    val southWestLat: Double,
    val southWestLng: Double
)


data class TeamSlotModel(
    val player: Player? = null,
    val isCurrentUser: Boolean = false
)


data class GameStatusDisplay(
    val text: String,
    val iconRes: Int,
)


data class GameState(

    val status: Status,
    val currentUserStatus: UserStatus,

    val maxPlayersPerTeam: Int,
    val isOrganizer: Boolean,

    val canStartGame: Boolean,

    val team1Players: List<Player>,
    val team2Players: List<Player>
) {

    enum class UserStatus {
        JOINED,
        INVITED,
        GAME_FULL,
        NOT_JOINED
    }

    enum class Status {
        DONE,
        IN_PROGRESS,
        WAITING,
        NONE
    }
}





data class GameScoreDetails(

    val scoreBoardTitle: String,
    val scoreBoardMessage: String,

    val role: GameUserRole,
    val scoreboardState: ScoreboardState,
    val validationState: ValidationState?,

    val status: String,
    val isMatchDone: Boolean,
    val isMatchFinished: Boolean,
    val isNoScoreProposed: Boolean,

    val isMyTurn: Boolean,
    val isWaitingForOpponent: Boolean,
    val didMyTeamPropose: Boolean,

    val officialScore: Pair<Int, Int>,
    val myScore: Pair<Int, Int>,
    val opponentScore: Pair<Int, Int>,

    val daysLeftForValidation: Int
)

enum class GameUserRole {
    TEAM1,
    TEAM2,
    SPECTATOR
}

enum class ScoreboardState {
    SPECTATOR,
    NO_SCORE_PROPOSED,
    WAITING_FOR_OPPONENT_VALIDATION,
    YOUR_TURN_TO_VALIDATE,
    FINISHED
}

enum class ValidationState {
    NOT_YOUR_TURN,
    FIRST_VALIDATION,
    COUNTER_VALIDATION
}

enum class MatchResult {
    WIN,
    LOSS,
    DRAW
}






enum class LocationScope {
    CITY,
    REGION,
    COUNTRY
}


data class OptimizedCountry(
    val name: String,
    val bounds: OptimizedBounds,
    val regions: Map<String, OptimizedRegion>
)

data class OptimizedRegion(
    val bounds: OptimizedBounds
)

data class OptimizedBounds(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double
)



enum class AccountState {
    NO_PLAYER_ACCOUNT,
    NO_ORGANIZER_ACCOUNT,
    SWITCH_TO_PLAYER,
    SWITCH_TO_ORGANIZER;

    fun titleText(): String {
        return when (this) {
            NO_PLAYER_ACCOUNT ->
                "Would you like to create a player account?"

            NO_ORGANIZER_ACCOUNT ->
                "Are you a professional who wants to organize a camp or a tournament?"

            SWITCH_TO_PLAYER ->
                "You are currently on your organizer account"

            SWITCH_TO_ORGANIZER ->
                "You are currently on your player account"
        }
    }

    fun buttonTitle(): String {
        return when (this) {
            NO_PLAYER_ACCOUNT ->
                "Create a player account"

            NO_ORGANIZER_ACCOUNT ->
                "Create an organizer account"

            SWITCH_TO_PLAYER ->
                "Switch to player account"

            SWITCH_TO_ORGANIZER ->
                "Switch to organizer account"
        }
    }
}


enum class AccountType {
    PLAYER,
    ORGANIZER
}


@Parcelize
data class OrganizerProfileData(
    val username: String,
    val feedCountry: String,
    val email: String
) : Parcelable
