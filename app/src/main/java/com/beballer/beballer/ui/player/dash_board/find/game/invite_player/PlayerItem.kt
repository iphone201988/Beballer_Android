package com.beballer.beballer.ui.player.dash_board.find.game.invite_player

import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.ui.player.dash_board.find.courts.ViewItem

sealed  class PlayerItem {

    data class Post(val players: Player) : PlayerItem()
    data object Loader : PlayerItem()
}