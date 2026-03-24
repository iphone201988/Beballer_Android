package com.beballer.beballer.data.api

import retrofit2.http.DELETE


object Constants {
    const val BASE_URL = "http://98.86.12.144:9000/api/v1/"
    const val IMAGE_URL = "https://d3dnkdkyhsfbca.cloudfront.net"
    const val IMAGE_URL1 = "https://d3dnkdkyhsfbca.cloudfront.net"

    var welcomeDialog = 0
    var userType = 1

    /**************** API LIST *****************/
    const val HEADER_API = "X-API-Key:lkcMuYllSgc3jsFi1gg896mtbPxIBzYkEL"
    const val MOBILE_LOGIN = "user/login"
    const val ON_BOARDING = "player/set-onboard-analytics"
    const val PLAYER_TEAM = "player/get-all-teams"
    const val CREATE_PROFILE = "player/create-player-profile"
    const val USER_PROFILE = "user/profile"
    const val USER_NAME_UNIQUE = "user/is-username-unique"
    const val USER_GET_POST = "post/get-posts"
    const val USER_POST_LIKE = "post/like-post"
    const val USER_POST_LIKE_COMMENT = "post/like-comment"
    const val USER_GET_POST_COMMENT = "post/get-post-comments"
    const val USER_POST_COMMENT = "post/comment-on-post"
    const val USER_CREATE_POST = "post/create-post"
    const val USER_SUBSCRIBE = "user/subscribe"
    const val REPORT_OR_DELETE_POST = "post/report-or-delete-post"
    const val GET_FOLLOWERS_FOLLOWING = "user/get-followers-following"
    const val GET_SUGGESTED_PLAYERS = "player/get-suggested-players"
    const val POST_PUBLISHER_ID = "post/get-post-by-puplisher-id/"
    const val PLAYER_POST = "post/get-post"
    const val USER_GET_USER_BY_ID = "user/get-user-by-id"
    const val GET_COURTS = "court/get-courts"
    const val GET_COURTS_BY_ID = "court/get-court/"
    const val NEW_COURT = "court/new"
    const val GAME_MAP_BOUNDS = "game/map-bounds"
    const val COURT_MAP_BOUNDS = "court/map-bounds"
    const val TICKET_MAP_BOUNDS = "ticket/map-bounds"
    const val TOURNAMENTS_MAP_BOUNDS = "tournament/map-bounds"
    const val CAMPS_MAP_BOUNDS = "camps/map-bounds"

    const val NEARBY_PLAYER = "player/get-nearby-players"

    const val CREATE_GAME = "game/create-game"

    const val MY_GAMES = "game/get-games"

    const val GET_GAME_BY_ID = "game/get-game/"

    const val ADD_RATING = "court/rate-court"


    const val  DELETE_GAME = "game/delete"


    const val ACCEPT_OR_REJECT = "game/accept-or-reject-invitation"

    const val START_GAME  = "game/start-game"

    const val LEAVE_GAME  = "game/leave-game"

    const val VALIDATE_GAME = "game/validate-score"

    const val  UPDATE_GAME_SCORE = "game/update-game-score"

    const val REMOVE_PLAYER = "game/remove-player"

    const val ADD_NEW_PLAYER = "game/add-player-to-game"

    const val GET_MESSAGES = "game/get-game-chat-messages/"


    const val REMOVE_REFEREE  = "game/remove-referee"

    const val ADD_REFEREE = "game/add-referee-to-game"


    const val TOP_PLAYERS = "player/get-top-players-by-progression"


    const val GET_PLAYER_BY_BOUNDS  = "player/get-players-by-bounds"

    const val ORGANIZER_PRO_CODE = "organiser/validate-pro-code"

    const val UNIQUE_NAME = "user/is-username-unique"


    const val CREATE_ORGANIZER = "organiser/create-organiser"

    const val EDIT_ORGANIZER = "organiser/create-organiser"

    const val CREATE_EVENT_TOURNAMENT = "event/create-event-tournament"

    const val ADD_ORGANIZER_CATEGORY = "event/add-or-organize-tournament-category"
}