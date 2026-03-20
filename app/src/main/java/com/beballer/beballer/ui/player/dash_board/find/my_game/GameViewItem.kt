package com.beballer.beballer.ui.player.dash_board.find.my_game

import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.MyGame
import com.beballer.beballer.data.model.MyGamesData
import com.beballer.beballer.ui.player.dash_board.find.courts.ViewItem

sealed  class GameViewItem {

    data class Post(val post: MyGame) : GameViewItem()
    data object Loader : GameViewItem()
}