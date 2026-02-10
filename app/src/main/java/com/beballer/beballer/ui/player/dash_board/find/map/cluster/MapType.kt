package com.beballer.beballer.ui.player.dash_board.find.map.cluster

import com.beballer.beballer.data.api.Constants

enum class MapType(val url: String, val tag: String) {
    COURT(Constants.COURT_MAP_BOUNDS, Constants.COURT_MAP_BOUNDS),
    GAME(Constants.GAME_MAP_BOUNDS, Constants.GAME_MAP_BOUNDS),
    TICKET(Constants.TICKET_MAP_BOUNDS, Constants.TICKET_MAP_BOUNDS),
    TOURNAMENT(Constants.TOURNAMENTS_MAP_BOUNDS, Constants.TOURNAMENTS_MAP_BOUNDS),
    CAMP(Constants.CAMPS_MAP_BOUNDS, Constants.CAMPS_MAP_BOUNDS),
    SEARCH(Constants.COURT_MAP_BOUNDS, "search")
}
