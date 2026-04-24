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
data class GameModes( var title: String ,  val modeId: Int =  0 , val apiValue: String = "")
data class SubscriptionModel( var title: String )
data class ChatModel( var message: String,var chatType: Boolean=false)
data class SettingsModel( var teamIcon: Int,var type:String , var colorCode:String)
data class CreateTournamentModel( var name: String , var id : String = "")
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



data class PlaceDetails(val name: String, val address: String, val location: LatLng, val city: String, val region: String, val country: String)


data class TournamentData(
    var eventId: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var name: String? = null,
    var level: String? = null,
    var city: String? = null,
    var format: String? = null,
    var priceRange: String? = null,
    var country: String? = null,
    var ageRange: String? = null,
    var description: String? = null,
    var address: String? = null,
    var region: String? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var usesBeballerForm: Boolean? = null,
    var hasCategories: Boolean? = null,
    var url : String ? =  null,

    // ✅ NEW FIELDS
    var courtsCount: Int? = null,
    var teamsCount: Int? = null,
    var poolsCount: Int? = null,

    var categoryId : String ? = null
)




@Parcelize
data class TournamentCategory(
    var tournamentName: String = "",
    var count : String  = "",
    var isSelected: Boolean = false,
    var tournamentAgeRange: String = "",
    var startDate: String? =  "",
    var endDate: String? = "",
    var courtsCount: String = "",
    var poolsCount: String = "",
    var teamsCount: String = "",
    var categoryID: String = "",
    var internalId: String = "",
    var eventID: String = "",

    var stability: String = "",
    var roundsCount: Int = 0,
    var finalTeamsCount: Int = 0,

    var isOrganised: Boolean = false,
    var hasSmallFinal: Boolean = false,


    ) : Parcelable


/// Get event api response


data class GetEventsApiResponse(
    val `data`: GetEventsData,
    val message: String,
    val success: Boolean
)

data class GetEventsData(
    val events: List<GetEventsDataEvent>,
    val pagination: GetEventsPagination
)

data class GetEventsDataEvent(
    val _id: String,
    val address: String,
    val categories: List<GetEventsCategory>,
    val city: String,
    val coordinates: List<Double>,
    val country: String,
    val createdAt: String,
    val description: String,
    val endDate: String,
    val eventPhotos: List<String>,
    val geohash: Any,
    val hasCategories: Boolean,
    val hasSponsors: Boolean,
    val id: String,
    val isVisible: Boolean,
    val isVisibleTo: List<String>,
    val lat: Double,
    val long: Double,
    val name: String,
    val organizers: List<GetEventsOrganizer>,
    val organizersCode: String,
    val organizersInfo: List<GetEventsOrganizersInfo>,
    val paymentStatus: Int,
    val referees: List<Any>,
    val refereesCode: String,
    val refereesInfo: List<Any>,
    val region: String,
    val shareLink: Any,
    val spectators: List<Any>,
    val spectatorsCode: String,
    val spectatorsInfo: List<Any>,
    val startDate: String,
    val type: String,
    val updatedAt: String
)

data class GetEventsPagination(
    val currentPage: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val totalCount: Int,
    val totalPages: Int
)

data class GetEventsCategory(
    val _id: String,
    val ageRange: String,
    val courts: List<GetEventsCourt>,
    val courtsCount: Int,
    val description: String,
    val endDate: String,
    val eventId: String,
    val format: String,
    val hasSmallFinal: Boolean,
    val id: String,
    val isOrganised: Boolean,
    val level: String,
    val name: String,
    val players: List<Any>,
    val poolsCount: Int,
    val priceRange: String,
    val registeredPlayers: List<Any>,
    val roundsCount: Int,
    val spectators: List<Any>,
    val startDate: String,
    val teamsCount: Int,
    val url: String,
    val usesBeballerForm: Boolean
)

data class GetEventsOrganizer(
    val collectionName: String,
    val id: String
)

data class GetEventsOrganizersInfo(
    val _id: String,
    val coordinates: List<Double>,
    val id: String,
    val profilePicture: String,
    val username: String,
    val verified: Boolean
)

data class GetEventsCourt(
    val _id: String,
    val id: String,
    val name: String,
    val number: Int,
    val stability: Any
)


// get eventDetail api Response

data class GetEventDetailsApiResponse(
    val `data`: GetEventDetailsData,
    val message: String,
    val success: Boolean
)

data class GetEventDetailsData(
    val event: GetEventDetailsEvent
)

data class GetEventDetailsEvent(
    val _id: String,
    val address: String,
    val categories: List<GetEventDetailsCategory>,
    val city: String,
    val coordinates: List<Double>,
    val country: String,
    val createdAt: String,
    val description: String,
    val endDate: String,
    val eventPhotos: List<String>,
    val geohash: Any,
    val hasCategories: Boolean,
    val hasSponsors: Boolean,
    val id: String,
    val isVisible: Boolean,
    val isVisibleTo: List<String>,
    val lat: Double,
    val long: Double,
    val name: String,
    val organizers: List<GetEventDetailsOrganizer>,
    val organizersCode: String,
    val organizersInfo: List<GetEventDetailsOrganizersInfo>,
    val paymentStatus: Int,
    val playersInfo: List<PlayersInfoX>,
    val referees: List<Any?>,
    val refereesCode: String,
    val refereesInfo: List<Any?>,
    val region: String,
    val shareLink: Any,
    val spectators: List<Any?>,
    val spectatorsCode: String,
    val spectatorsInfo: List<Any?>,
    val sponsors: List<Any?>,
    val startDate: String,
    val type: String,
    val updatedAt: String
)

data class GetEventDetailsCategory(
    val _id: String,
    val ageRange: String,
    val allMatches: List<AllMatche>,
    val allPoolMatches: List<AllPoolMatche>,
    val courts: List<GetEventDetailsCourt>,
    val courtsCount: Int,
    val description: String,
    val endDate: String,
    val eventId: String,
    val finalsMatches: List<Any?>,
    val format: String,
    val hasSmallFinal: Boolean,
    val id: String,
    val isOrganised: Boolean,
    val level: String,
    val name: String,
    val players: List<GetEventDetailsPlayer>,
    val poolMatches: List<PoolMatche>,
    val pools: List<Pool>,
    val poolsCount: Int,
    val priceRange: String,
    val registeredPlayers: List<Any?>,
    val roundsCount: Int,
    val spectators: List<Any?>,
    val startDate: String,
    val teamsCount: Int,
    val url: String,
    val usesBeballerForm: Boolean
)

data class GetEventDetailsOrganizer(
    val collectionName: String,
    val id: String
)

data class GetEventDetailsOrganizersInfo(
    val _id: String,
    val city: Any,
    val coordinates: List<Double>,
    val country: Any,
    val id: String,
    val profilePicture: String,
    val username: String,
    val verified: Boolean
)

data class PlayersInfoX(
    val _id: String,
    val badge: String,
    val city: String,
    val country: String,
    val countryCode: String,
    val email: String,
    val firstName: String,
    val gender: String,
    val hasOrganizerAccount: Boolean,
    val id: String,
    val lastName: String,
    val location: Location,
    val profilePicture: String,
    val region: String,
    val score: Int,
    val sector: String,
    val totalProgression: Int,
    val username: String,
    val verified: Boolean
)

data class AllMatche(
    val _id: String,
    val courtId: String,
    val courtName: String,
    val date: Any,
    val id: String,
    val number: Int,
    val pool: String,
    val round: Int,
    val scorePending: Boolean,
    val scoreTeam1: Int,
    val scoreTeam2: Int,
    val team1: Team1,
    val team2: Team1
)

data class AllPoolMatche(
    val _id: String,
    val courtId: String,
    val courtName: String,
    val date: Any,
    val id: String,
    val number: Int,
    val pool: String,
    val round: Int,
    val scorePending: Boolean,
    val scoreTeam1: Int,
    val scoreTeam2: Int,
    val team1: Team1,
    val team2: Team1
)

data class GetEventDetailsCourt(
    val _id: String,
    val id: String,
    val name: String,
    val number: Int,
    val stability: Any
)

data class GetEventDetailsPlayer(
    val collectionName: String,
    val id: String
)

data class PoolMatche(
    val matches: List<Matche>,
    val pool: String,
    val poolLabel: String
)

data class Pool(
    val pool: String,
    val poolLabel: String,
    val teams: List<GetEventDetailsTeam>
)

data class Team1(
    val teamId: String,
    val teamName: String
)

data class Matche(
    val _id: String,
    val courtId: String,
    val courtName: String,
    val date: Any,
    val id: String,
    val number: Int,
    val pool: String,
    val round: Int,
    val scorePending: Boolean,
    val scoreTeam1: Int,
    val scoreTeam2: Int,
    val team1: Team1,
    val team2: Team1
)

data class GetEventDetailsTeam(
    val code: String,
    val goalsReceived: Int,
    val goalsScored: Int,
    val logo: String,
    val players: List<Player>,
    val playersInfo: List<PlayersInfo>,
    val points: Int,
    val rank: Int,
    val teamId: String,
    val teamName: String
)

data class PlayersInfo(
    val collectionName: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val profilePicture: String,
    val username: String,
    val verified: Boolean
)

data class Location(
    val coordinates: List<Double>,
    val type: String
)