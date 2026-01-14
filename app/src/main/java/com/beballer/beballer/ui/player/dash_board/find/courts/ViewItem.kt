package com.beballer.beballer.ui.player.dash_board.find.courts

import com.beballer.beballer.data.model.GetCourtData


sealed class ViewItem {
    data class Post(val post: GetCourtData) : ViewItem()
    data object Loader : ViewItem()
}