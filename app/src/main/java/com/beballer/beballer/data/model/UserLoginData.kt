package com.beballer.beballer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** common response ***/
data class CommonResponse(
    val message: String?, val success: Boolean?
)

data class PostCommentResponse(
    val commentId: String?, val message: String?, val success: Boolean?
)


/** login and signup response ***/
data class LoginApiResponse(val data: Data?, val message: String?, val success: Boolean?)

data class Data(val token: String, val user: User?)

data class User(
    val _id: String?,
    val birthDate: String?,
    val city: String?,
    val country: String?,
    val favoriteProTeam: FavoriteProTeam,
    val feedCountry: String?,
    val firstName: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val height: String?,
    val id: String?,
    val isOnboardAnalyticsDone: Boolean?,
    val isProfileCompleted: Boolean?,
    val hasOrganizerAccount: Boolean?,
    val hasPlayerAccount: Boolean?,
    val lastName: String?,
    val playPositionId: String?,
    val position: String?,
    val username: String?,
    val profilePicture: String?,
    val progression: List<Any>,
    val rank: Int?,
    val rankRegion: Int?,
    val rankSector: Int?,
    val recutersViewed: String?,
    val referralCode: String?,
    val region: String?,
    val score: Int?,
    val sector: String?,
    val totalProgression: Int?,
    val verified: Boolean?,
    val lat: Double,
    val long: Double,
    val setProfilePopup: Boolean,
    val setSettingsPopup: Boolean,

    )

data class FavoriteProTeam(
    val ref: Ref?
)

data class Ref(
    val id: String?
)

/** user profile **/
data class UserProfile(
    val `data`: ProfileData?, val message: String?, val success: Boolean?
)

data class ProfileData(
    val user: ProfileDataUser?
)

data class ProfileDataUser(
    val _id: String?,
    val badgeUri: String?,
    val birthDate: String?,
    val city: String?,
    val country: String?,
    val countryCode: String?,
    val favoriteProTeam: UserFavoriteProTeam?,
    val feedCountry: String?,
    val firstName: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val gender: String?,
    val height: Int?,
    val profileDescription: String?,
    val id: String?,
    val isOnboardAnalyticsDone: Boolean?,
    val isProfileCompleted: Boolean?,
    val hasPlayerAccount: Boolean?,
    val lastName: String?,
    val lat: Double?,
    val long: Double?,
    val playPositionId: Int?,
    val position: String?,
    val profilePicture: String?,
    val progression: List<Any?>,
    val rank: Int?,
    val rankRegion: Int?,
    val rankSector: Int?,
    val recutersViewed: String?,
    val referralCode: String?,
    val region: String?,
    val score: Int?,
    val sector: String?,
    val setProfilePopup: Boolean?,
    val setSettingsPopup: Boolean?,
    val totalProgression: Int?,
    val username: String?,
    val verified: Boolean,
    val hasOrganizerAccount: Boolean?,
    val isAdmin: Boolean?,
    val isDeleted: Boolean?,

    )

data class UserFavoriteProTeam(
    val _id: String?, val id: String?, val imageURL: String?, val name: String?
)

/** get player team **/
data class GetPlayerTeamResponse(
    val `data`: List<PlayerTeamData?>, val message: String?, val success: Boolean?
)

data class PlayerTeamData(
    val _id: String?,
    val coordinates: List<Double?>,
    val id: String?,
    val imageURL: String?,
    val name: String?,
    val type: String?,
    val url: String?,
    var check: Boolean = false
)

/** get user post response **/
data class GetUserPostResponse(
    val data: List<GetUserPostData?>?,
    val message: String?,
    val pagination: Pagination?,
    val success: Boolean?


)

@Parcelize
data class GetUserPostData(
    val __v: Int?,
    val _id: String?,
    var commentCount: Int?,
    var contentType: String?,
    val country: String?,
    var currentUserLikeCount: Int?,
    val date: String?,
    val description: String?,
    val event: GetUserPostEvent?,
    val id: String?,
    val image: String?,
    val isCommentedByCurrentUser: Boolean?,
    val isDeleted: Boolean?,
    val isFeed: Boolean?,
    val field: Field?,
    val isLikedByCurrentUser: Boolean?,
    val isSharedByCurrentUser: Boolean?,
    var isSubscribed: Boolean?,
    var likesCount: Int?,
    val postContentHeight: String?,
    val postContentWidth: String?,
    val publisherData: PublisherData?,
    val score: Int?,
    val video: String?,
    val court: UserPostCourt?,
    val game: Game?,
    val role: Int?,
    val proGame: String?,
    val stability: Int?

) : Parcelable

@Parcelize
data class Pagination(val currentPage: Int?, val totalPages: Int?) : Parcelable

@Parcelize
data class GetUserPostEvent(
    val _id: String?,
    val address: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val createdAt: String?,
    val discountCode: String?,
    val endDate: String?,
    val formats: String?,
    val geohash: String?,
    val hasCategories: Boolean?,
    val hasSponsors: Boolean?,
    val isVisible: Boolean?,
    val name: String?,
    val organizersCode: String?,
    val paymentStatus: Int?,
    val referees: List<EventReferee?>?,
    val refereesCode: String?,
    val region: String?,
    val shareLink: String?,
    val spectatorsCode: String?,
    val startDate: String?,
    val type: String?,
    val eventPhotos: List<String?>?,
    val lat: Double?,
    val long: Double?,

    ) : Parcelable

@Parcelize
data class PublisherData(
    val _id: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val username: String?,
    val verified: Boolean?

) : Parcelable

@Parcelize
data class UserPostCourt(
    val _id: String?,
    val accessibility: String?,
    val address: String?,
    val areDimensionsStandard: Boolean?,
    val boardType: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val createdAt: String?,
    val description: String?,
    val floorType: String?,
    val geohash: String?,
    val grade: Double?,
    val hasWaterPoint: Boolean?,
    val hoopsCount: Int?,
    val id: String?,
    val isWomanFriendly: Boolean?,
    val name: String?,
    val netType: String?,
    val photos: List<String?>?,
    val region: String?,
    val zipCode: String?
) : Parcelable


@Parcelize
data class Game(
    val _id: String?,
    val createdAt: String?,
    val date: String?,
    val `field`: Field?,
    val hasAcceptedInvitationReferee: Boolean?,
    val hasAcceptedInvitationTeam1: List<Boolean>?,
    val hasAcceptedInvitationTeam2: List<Boolean>?,
    val id: String?,
    val isAutoRefereeing: Boolean?,
    val mode: Int?,
    val organizer: Organizer?,
    val scoreTeam1: Int?,
    val scoreTeam2: Int?,
    val startDate: String?,
    val totalJoinedPlayers: String?,
    val status: String?,
    val team1Players: List<Team1Player>?,
    val team1ScoreTeam1: Int?,
    val team1ScoreTeam2: Int?,
    val team2Players: List<Team1Player>?,
    val team2ScoreTeam1: Int?,
    val team2ScoreTeam2: Int?,
    val teamToValidate: Int?,
    val type: String?,
    val visible: Boolean?
) : Parcelable

@Parcelize
data class Field(
    val _id: String?,
    val city: String?,
    val country: String?,
    val id: String?,
    val name: String?,
    val photos: List<String>,
    val postalCode: String?,
    val region: String?
) : Parcelable

@Parcelize
data class Organizer(
    val ref: GameRef?
) : Parcelable

@Parcelize
data class Team1Player(
    val accepted: Boolean?, val collectionName: String?, val id: String?
) : Parcelable

@Parcelize
data class GameRef(
    val collectionName: String?, val id: String?
) : Parcelable


@Parcelize

data class EventReferee(
    val collectionName: String?, val id: String?
) : Parcelable


/** post comment response **/
data class GetPostCommentResponse(
    val `data`: List<PostCommentData?>?,
    val message: String?,
    val pagination: Pagination?,
    val success: Boolean?
)

data class PostCommentData(
    val _id: String?,
    val comment: String?,
    val createdAt: String?,
    var currentUserLikeCount: Int?,
    val id: String?,
    val isDeleted: Boolean?,
    var likeCount: Int?,
    val postId: String?,
    val publisherData: CommentData?
)

data class CommentData(
    val _id: String?,
    val firstName: String?,
    val id: String?,
    val lastName: Any?,
    val profilePicture: String?,
    val username: String?,
    val verified: Boolean?
)

/** following response **/
data class FollowingResponse(
    val `data`: FollowingData?, val message: String?, val success: Boolean?
)

data class FollowingData(
    val followingUser: List<FollowingUser>?, val pagination: Pagination?
)

data class FollowingUser(
    val _id: String?,
    val badge: String?,
    val badgeUri: String?,
    val city: String?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val score: Int?,
    val username: String?

)

/** following response **/
data class FollowersResponse(
    val `data`: FollowersData?, val message: String?, val success: Boolean?
)

data class FollowersData(
    val followerUser: List<FollowerUser>?, val pagination: Pagination?
)

data class FollowerUser(
    val _id: String?,
    val badge: String?,
    val badgeUri: Any?,
    val city: String?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val score: Int?,
    val username: String?
)

/** get suggested response **/

data class GetSuggestedResponse(
    val `data`: SuggestedData?, val message: String?, val success: Boolean?
)

data class SuggestedData(
    val currentPage: Int?,
    val totalCount: Int?,
    val totalPages: Int?,
    val users: List<SuggestedUser?>?
)

data class SuggestedUser(
    val _id: String?,
    val city: String?,
    val country: String?,
    val distance: Double?,
    val firstName: String?,
    val id: String?,
    var isSubscribed: Boolean?,
    val lastName: String?,
    val lat: Double?,
    val long: Double?,
    val profilePicture: String?,
    val score: Int?,
    val username: String?,
)

/** get player profile response **/
@Parcelize
data class PlayerProfileResponse(
    val `data`: List<PlayerData?>?,
    val message: String?,
    val pagination: Pagination?,
    val success: Boolean?
) : Parcelable

@Parcelize
data class PlayerData(
    val _id: String?,
    val contentType: String?,
    val description: String?,
    val id: String?,
    val image: String?,
    val postContentHeight: String?,
    val postContentWidth: String?,
    val video: String?
) : Parcelable

/** get player post by id  response **/
data class PlayerPostBYIdResponse(
    val `data`: PlayerPostData?, val message: String?, val success: Boolean?
)

data class PlayerPostData(
    val __v: Int?,
    val _id: String?,
    val commentCount: Int?,
    val contentType: String?,
    val currentUserLikeCount: Int?,
    val date: String?,
    val description: String?,
    val id: String?,
    val image: String?,
    val isCommentedByCurrentUser: Boolean?,
    val isDeleted: Boolean?,
    val isFeed: Boolean?,
    val isLikedByCurrentUser: Boolean?,
    val isReported: Boolean?,
    val isSharedByCurrentUser: Boolean?,
    val isSubscribed: Boolean?,
    val likesCount: Int?,
    val postContentHeight: String?,
    val postContentWidth: String?,
    val publisherData: PlayerPublisherData?,
    val score: Int?,
    val video: String?
)

data class PlayerPublisherData(
    val _id: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val username: String?,
    val verified: Boolean?
)


/** Player Profile  response **/
data class PlayerProfileByIdResponse(
    val `data`: ProfileByIdData?, val message: String?, val success: Boolean?
)

data class ProfileByIdData(
    val user: ProfileByUser?
)

data class ProfileByUser(
    val _id: String?,
    val badgeUri: Any?,
    val birthDate: String?,
    val city: String?,
    val country: String?,
    val countryCode: String?,
    val favoriteProTeam: ProfileByFavoriteProTeam?,
    val feedCountry: Any?,
    val firstName: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val gender: String?,
    val hasOrganizerAccount: Boolean?,
    val height: Int?,
    val id: String?,
    val isAdmin: Boolean?,
    val isDeleted: Boolean?,
    val isOnboardAnalyticsDone: Boolean?,
    val isProfileCompleted: Boolean?,
    val isSubscribed: Boolean?,
    val lastName: String?,
    val lat: Double?,
    val long: Double?,
    val playPositionId: Int?,
    val position: String?,
    val profileDescription: Any?,
    val profilePicture: String?,
    val progression: List<Any?>?,
    val rank: Int?,
    val rankRegion: Int?,
    val rankSector: Int?,
    val recutersViewed: String?,
    val referralCode: Any?,
    val region: Any?,
    val score: Int?,
    val sector: String?,
    val setProfilePopup: Boolean?,
    val setSettingsPopup: Boolean?,
    val totalProgression: Int?,
    val username: String?,
    val verified: Boolean?


)

data class ProfileByFavoriteProTeam(
    val _id: String?, val id: String?, val imageURL: String?, val name: String?
)


/** Get Court Api response **/

@Parcelize
data class GetCourtApiResponse(
    val courts: List<GetCourtData?>?,
    val message: String?,
    val pagination: CourtPagination?,
    val success: Boolean?
) : Parcelable

@Parcelize
data class GetCourtData(
    val _id: String?,
    val address: String?,
    val createdAt: String?,
    val distance: Double?,
    val grade: Double?,
    val hoopsCount: Int?,
    val id: String?,
    val rating: Double?,
    val lat: Double?,
    val long: Double?,
    val name: String?,
    val photos: List<String?>?
) : Parcelable

@Parcelize
data class CourtPagination(
    val currentPage: Int?, val totalCount: Int?, val totalPages: Int?
) : Parcelable


/** Get Court Api response **/
data class GetCourtByIdResponse(
    val `data`: DataById?, val message: String?, val success: Boolean?
)

data class DataById(
    val court: CourtDataById?
)

@Parcelize
data class CourtDataById(
    val _id: String?,
    val accessibility: String?,
    val address: String?,
    val areDimensionsStandard: Boolean?,
    val boardType: String?,
    val city: String?,
    val country: String?,
    val createdAt: String?,
    val description: String?,
    val distance: Double?,
    val floorType: String?,
    val grade: Double?,
    val rating: Double?,
    val hasWaterPoint: Boolean?,
    val hoopsCount: Int?,
    val id: String?,
    val isWomanFriendly: Boolean?,
    val lat: Double?,
    val level: String?,
    val long: Double?,
    val name: String?,
    val netType: String?,
    val photos: List<String>?,
    val userInformation: UserInformation?,
    val zipCode: String?
) : Parcelable

@Parcelize
data class UserInformation(
    val _id: String?, val firstName: String?, val id: String?, val lastName: String?
) : Parcelable


data class AddCourtDataClass(
    val courtId: String?, val message: String?, val success: Boolean?
)

data class UpdateCourtData(
    val `data`: UpdateCourt, val message: String, val success: Boolean
)

data class UpdateCourt(
    val message: String
)

/**
 * Get Map Bounds response
 */
data class GetMapBoundData(
    val bounds: Bounds?,
    val count: Int?,
    val courts: List<MapCourt>?,
    val message: String?,
    val success: Boolean?
)

data class Bounds(
    val northEast: NorthEast?, val southWest: SouthWest?
)

data class MapCourt(
    val _id: String?,
    val address: String?,
    val city: String?,
    val country: String?,
    val distance: Double?,
    val geohash: String?,
    val hoopsCount: Int?,
    val id: String?,
    val lat: Double?,
    val grade: Double?,
    val long: Double?,
    val name: String?,
    val photos: List<String?>?,
    val postalCode: String?,
    val rating: Double?,
    val region: String?

)

data class NorthEast(
    val lat: Double?, val lng: Double?
)

data class SouthWest(
    val lat: Double?, val lng: Double?
)


/**
 * search map api response
 */
data class SearchCourtApiData(
    val count: Int?, val courts: List<MapCourt?>?, val message: String?, val success: Boolean?
)


/**
 * Get game Map Bounds response
 */
data class GetGameMapBoundData(
    val `data`: GameMapData?, val message: String?, val success: Boolean?
)

data class GameMapData(
    val games: List<GameData>?, val pagination: Pagination?
)

data class GameData(
    val _id: String?,
    val createdAt: String?,
    val date: String?,
    val `field`: GameField?,
    val hasAcceptedInvitationReferee: Boolean?,
    val hasAcceptedInvitationTeam1: List<Boolean>?,
    val hasAcceptedInvitationTeam2: List<Boolean>?,
    val id: String?,
    val isAutoRefereeing: Boolean?,
    val mode: Int?,
    val organizer: Organizer?,
    val scoreTeam1: Int?,
    val scoreTeam2: Int?,
    val startDate: String?,
    val totalJoinedPlayers: String?,
    val status: String?,
    val team1Players: List<Team1Player>?,
    val team1ScoreTeam1: Int?,
    val team1ScoreTeam2: Int?,
    val team2Players: List<Team1Player>?,
    val team2ScoreTeam1: Int?,
    val team2ScoreTeam2: Int?,
    val teamToValidate: Int?,
    val type: String?,
    val visible: Boolean?


)


data class GameField(
    val _id: String?,
    val address: String?,
    val id: String?,
    val lat: Double?,
    val long: Double?,
    val name: String?,
    val photos: List<String?>?
)


/**
 * Get Ticket Map Bounds response
 */
data class GetTicketMapBoundData(
    val `data`: MapCourt?, val message: String?, val success: Boolean?
)

/**
 * Get Tournaments Map Bounds response
 */
data class GetTournamentsMapBoundData(
    val `data`: TournamentsCourt?, val message: String?, val success: Boolean?

)

data class TournamentsCourt(
    val bounds: Bounds?,
    val count: Int?,
    val events: List<TournamentsEvent>?
)


data class TournamentsEvent(
    val _id: String?,
    val address: String?,
    val categories: List<TournamentsCategory?>?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val createdAt: String?,
    val description: String?,
    val endDate: String?,
    val eventPhotos: List<String?>?,
    val geohash: String?,
    val hasCategories: Boolean?,
    val hasSponsors: Boolean?,
    val id: String?,
    val isVisible: Boolean?,
    val isVisibleTo: List<String?>?,
    val lat: Double?,
    val long: Double?,
    val name: String?,
    val organizers: List<TournamentsOrganizer?>?,
    val organizersCode: String?,
    val organizersInfo: List<OrganizersInfo?>?,
    val paymentStatus: Int?,
    val referees: List<TournamentReferee?>?,
    val refereesCode: String?,
    val refereesInfo: List<RefereesInfo?>?,
    val region: String?,
    val shareLink: Any?,
    val spectators: List<SpectatorX?>?,
    val spectatorsCode: String?,
    val spectatorsInfo: List<SpectatorsInfo?>?,
    val sponsors: List<Sponsor>?,
    val startDate: String?,
    val type: String?,
    val updatedAt: String?

)

data class TournamentsOrganizer(
    val collectionName: String?,
    val id: String?
)



data class TournamentsCategory(
    val _id: String?,
    val ageRange: String?,
    val courts: List<Court>?,
    val courtsCount: Int?,
    val description: String?,
    val endDate: String?,
    val eventId: String?,
    val format: String?,
    val hasSmallFinal: Boolean?,
    val id: String?,
    val isOrganised: Boolean?,
    val level: String?,
    val name: String?,
    val players: List<TourPlayer>?,
    val poolsCount: Int?,
    val priceRange: String?,
    val registeredPlayers: List<RegisteredPlayer>?,
    val roundsCount: Int?,
    val spectators: List<SpectatorX>?,
    val startDate: String?,
    val teamsCount: Int?,
    val url: String?,
    val usesBeballerForm: Boolean?
)



data class TournamentReferee(
    val collectionName: String?,
    val id: String?
)

data class RefereesInfo(
    val _id: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val username: String?,
    val verified: Boolean?
)

data class SpectatorX(
    val collectionName: String?,
    val id: String?
)

data class SpectatorsInfo(
    val _id: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val username: String?,
    val verified: Boolean?
)

data class Sponsor(
    val eventId: String?,
    val id: String?,
    val imageURL: String?,
    val name: String?,
    val number: Int?,
    val url: String?
)


data class TourPlayer(
    val collectionName: String?,
    val id: String?
)

data class RegisteredPlayer(
    val collectionName: String?,
    val id: String?
)




/**
 * Get Camps Map Bounds response
 */
data class GetCampsMapBoundData(
    val `data`: MapCourt?, val message: String?, val success: Boolean?
)

/**
 * get player api response
 */

@Parcelize
data class GetPlayersApiResponse(
    val limit: Int?,
    val message: String?,
    val page: Int?,
    val players: List<Player?>?,
    val success: Boolean?,
    val totalCount: Int?,
    val totalPages: Int?
) : Parcelable

@Parcelize
data class Player(
    val _id: String?,
    val city: String?,
    val country: String?,
    val distance: Double?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val lat: Double?,
    val long: Double?,
    val profilePicture: String?,
    val score: Int?,
    val username: String?,
    val accepted: Boolean? = null
) : Parcelable

/**
 * create game api response
 */


data class CreateGameApiResponse(
    val `data`: CreateGameData?, val message: String?, val success: Boolean?
)

data class CreateGameData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val date: String?,
    val `field`: CreateField?,
    val hasAcceptedInvitationReferee: Boolean?,
    val hasAcceptedInvitationTeam1: List<Any?>?,
    val hasAcceptedInvitationTeam2: List<Any?>?,
    val id: String?,
    val isAutoRefereeing: Boolean?,
    val isDeleted: Boolean?,
    val isVisible: Boolean?,
    val mode: Int?,
    val organizer: CreateOrganizer?,
    val referee: Referee?,
    val scoreTeam1: Int?,
    val scoreTeam2: Int?,
    val status: String?,
    val team1Players: List<CreateTeam1Player>?,
    val team1ScoreTeam1: Int?,
    val team1ScoreTeam2: Int?,
    val team2Players: List<CreateTeam1Player>?,
    val team2ScoreTeam1: Int?,
    val team2ScoreTeam2: Int?,
    val teamToValidate: Int?,
    val updatedAt: String?,
    val visible: Boolean?
)

data class CreateField(
    val ref: CreateRef?
)

data class CreateOrganizer(
    val ref: CreateRef?
)

data class Referee(
    val ref: CreateRef?
)

data class CreateTeam1Player(
    val accepted: Boolean?, val collectionName: String?, val id: String?
)

data class CreateRef(
    val collectionName: String?, val id: String?
)

/**
 * my games  api response
 */


data class MyGamesApiResponse(
    val `data`: MyGamesData?, val message: String?, val success: Boolean?
)

data class MyGamesData(
    val games: List<MyGame>?, val pagination: MyGamePagination?
)

data class MyGame(
    val _id: String?,
    val createdAt: String?,
    val date: String?,
    val `field`: MyGameField?,
    val id: String?,
    val mode: Int?,
    val status: String?,
    val team1Players: List<GameTeam1Player>?,
    val team2Players: List<GameTeam1Player>?,
    val totalJoinedPlayers: String?,
    val yourRating: Double?

)

data class MyGamePagination(
    val currentPage: Int?, val totalPages: Int?, val totalRecords: Int?
)

data class MyGameField(
    val _id: String?,
    val address: String?,
    val id: String?,
    val lat: Double?,
    val long: Double?,
    val name: String?,
    val photos: List<String?>?
)

data class GameTeam1Player(
    val accepted: Boolean?, val collectionName: String?, val id: String?
)


/**
 * game details by id  api response
 */


data class GetGameDetailsApiResponse(
    val game: GameDetail?, val message: String?, val success: Boolean?
)

data class GameDetail(
    val _id: String?,
    val date: String?,
    val `field`: GameDetailField?,
    val hasAcceptedInvitationTeam1: List<String?>?,
    val hasAcceptedInvitationTeam2: List<String?>?,
    val id: String?,
    val isAutoRefereeing: Boolean?,
    val mode: Int?,
    val creationDate: String?,
    val scoreTeam1: Int,
    val scoreTeam2: Int,
    val team2ScoreTeam1: Int,
    val team2ScoreTeam2: Int,
    val teamToValidate: Int,
    val team1ScoreTeam1: Int,
    val team1ScoreTeam2: Int,
    val organizer: GameDetailOrganizer?,
    val referee: GameDetailReferee?,
    val status: String?,
    val team1Players: List<GameDetailTeam1Player?>?,
    val team2Players: List<Team2Player?>?
)

data class GameDetailField(
    val _id: String?,
    val accessibility: String?,
    val address: String?,
    val areDimensionsStandard: Boolean?,
    val boardType: String?,
    val city: String?,
    val contributor: Contributor?,
    val description: String?,
    val floorType: String?,
    val grade: Int?,
    val hasWaterPoint: Boolean?,
    val hoopsCount: Int?,
    val id: String?,
    val latitude: Double?,
    val level: Any?,
    val longitude: Double?,
    val name: String?,
    val netType: String?,
    val photos: List<String?>?,
    val rating: Double?
)

data class GameDetailOrganizer(
    val _id: String?,
    val city: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val username: String?
)

data class GameDetailReferee(
    val _id: String?, val id: String?, val username: String
)

data class GameDetailTeam1Player(
    val _id: String?,
    val accepted: Boolean?,
    val city: String?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val score: Int?,
    val username: String?
)

data class Team2Player(
    val _id: String?,
    val accepted: Boolean?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val score: Int?
)

data class Contributor(
    val _id: String?,
    val city: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val username: String?
)


data class SimpleApiResponse(
    val message: String?, val success: Boolean?
)


// get chat api response

data class GetChatApiResponse(
    val `data`: ChatData, val message: String, val success: Boolean
)

data class ChatData(
    val messages: List<Message>, val pagination: ChatPagination
)

data class Message(
    val _id: String,
    val chatGroupId: String,
    val createdAt: String,
    val isDeleted: Boolean,
    val isRead: Boolean,
    val message: String,
    val senderId: String,
    val senderImage: String,
    val senderUsername: String,
    val updatedAt: String,
    var chatType: Boolean = false
)

data class ChatPagination(
    val hasNextPage: Boolean, val limit: Int, val page: Int, val total: Int, val totalPages: Int
)

// player top ranking

data class TopRankingApiResponse(
    val `data`: RankingData, val message: String, val success: Boolean
)

data class RankingData(
    val `1st`: St, val `2nd`: Nd, val `3rd`: Rd
)

data class St(
    val _id: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val profilePicture: String,
    val score: Int,
    val totalProgression: Int,
    val username: String
)

data class Nd(
    val _id: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val profilePicture: String,
    val score: Int,
    val totalProgression: Int,
    val username: String
)

data class Rd(
    val _id: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val profilePicture: String,
    val score: Int,
    val totalProgression: Int,
    val username: String
)


// player by bound api response

data class PlayerByBoundApiResponse(
    val `data`: PlayerBoundData, val message: String, val success: Boolean
)

data class PlayerBoundData(
    val pagination: PlayerPagination, val players: List<BoundPlayer>
)

data class PlayerPagination(
    val currentPage: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val totalCount: Int,
    val totalPages: Int
)

data class BoundPlayer(
    val _id: String,
    val city: String,
    val country: String,
    val firstName: String,
    var isCurrentUser: Boolean = false,
    val id: String,
    val lastname: String,
    val lastName: String,
    val lat: Double,
    val long: Double,
    val profilePicture: String,
    val rank: Int,
    val score: Int,
    val totalProgression: Int,
    val username: String
)


data class CreateTournamentApiResponse(
    val event: Event, val message: String, val success: Boolean,
    val categoryId : String?,
    val Category_id : String?
)

data class Event(
    val _id: String,
    val address: String,
    val categories: List<Any>,
    val city: String,
    val coordinates: List<Double>,
    val country: String,
    val createdAt: String,
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
    val organizers: List<TournamentOrganizer>,
    val organizersCode: String,
    val organizersInfo: List<OrganizersInfo>,
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

data class TournamentOrganizer(
    val collectionName: String, val id: String
)

data class OrganizersInfo(
    val _id: String?,
    val city: String?,
    val coordinates: List<Double?>?,
    val country: String?,
    val firstName: String?,
    val id: String?,
    val lastName: String?,
    val profilePicture: String?,
    val username: String?,
    val verified: Boolean?
)

/// create category api response

@Parcelize
data class CreateCategoryApiResponse(
    val `data`: CategoryData, val message: String, val success: Boolean
) : Parcelable

@Parcelize
data class CategoryData(
    val category: Category
) : Parcelable

@Parcelize
data class Category(
    val __v: Int,
    val _id: String,
    val ageRange: String,
    val courts: List<Court>,
    val courtsCount: Int,
    val createdAt: String,
    val description: String,
    val endDate: String,
    val eventId: String,
    val finalTeamsCount: Int,
    val format: String,
    val hasSmallFinal: Boolean,
    val id: String,
    val isDeleted: Boolean,
    val isOrganised: Boolean,
    val level: String,
    val lookingForATeamPlayers: List<String>,
    val name: String,
    val players: List<String>,
    val playersInfo: List<String>,
    val poolsCount: Int,
    val priceRange: String,
    val registeredPlayers: List<String>,
    val registeredPlayersInfo: List<String>,
    val roundsCount: Int,
    val spectators: List<String>,
    val stability: String,
    val startDate: String,
    val teams: List<Team>,
    val teamsCount: Int,
    val updatedAt: String,
    val url: String,
    val usesBeballerForm: Boolean

) : Parcelable


@Parcelize
data class Court(
    val __v: Int?,
    val _id: String?,
    val categoryId: String?,
    val createdAt: String?,
    val id: String?,
    val isDeleted: Boolean?,
    var name: String?,
    val number: Int?,
    val stability: String?,
    val updatedAt: String?
) : Parcelable


@Parcelize
data class Team(
    val __v: Int,
    val _id: String,
    val categoryId: String,
    val code: String,
    val createdAt: String,
    val eventId: String,
    val goalsReceived: Int,
    val goalsScored: Int,
    val id: String,
    val isDeleted: Boolean,
    val isReadyToCreateGames: Boolean,
    val name: String,
    val number: Int,
    val players: List<String>,
    val pool: String,
    val score: Int,
    val updatedAt: String
) : Parcelable


// update category api response

data class UpdateCategoryApiResponse(
    val `data`: UpdateCategoryData, val message: String, val success: Boolean
)

data class UpdateCategoryData(
    val courts: List<CategoryCourt>
)

data class CategoryCourt(
    val __v: Int,
    val _id: String,
    val categoryId: String,
    val createdAt: String,
    val id: String,
    val isDeleted: Boolean,
    val name: String,
    val number: Int,
    val stability: Any,
    val updatedAt: String
)