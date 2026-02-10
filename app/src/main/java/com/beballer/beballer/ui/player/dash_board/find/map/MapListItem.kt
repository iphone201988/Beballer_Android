import com.beballer.beballer.data.model.GameData
import com.beballer.beballer.data.model.MapCourt
import com.beballer.beballer.ui.interfacess.MapMarkerItem

sealed class MapListItem : MapMarkerItem {
    data class Court(val data: MapCourt) : MapListItem() {
        override val id = data.id!!
        override val lat = data.lat!!
        override val lng = data.long!!
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }
    data class Game(val data: GameData) : MapListItem() {
        override val id = data.id!!
        override val lat = data.field?.lat!!
        override val lng = data.field?.lat!!
        override val title = data.field?.name ?: ""
        override val address = data.field?.address ?: ""
    }
    data class Ticket(val data: MapCourt) : MapListItem() {
        override val id = data.id!!
        override val lat = data.lat!!
        override val lng = data.long!!
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }
    data class Tournament(val data: MapCourt) : MapListItem() {
        override val id = data.id!!
        override val lat = data.lat!!
        override val lng = data.long!!
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }
    data class Camp(val data: MapCourt) : MapListItem() {
        override val id = data.id!!
        override val lat = data.lat!!
        override val lng = data.long!!
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }
}

