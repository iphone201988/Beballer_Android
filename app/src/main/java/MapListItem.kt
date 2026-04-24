import com.beballer.beballer.data.model.GameData
import com.beballer.beballer.data.model.MapCourt
import com.beballer.beballer.data.model.TournamentsEvent
import com.beballer.beballer.ui.interfacess.MapMarkerItem

sealed class MapListItem : MapMarkerItem {
    data class Court(val data: MapCourt) : MapListItem() {
        override val id = data.id ?: ""
        override val lat = data.lat ?: 0.0
        override val lng = data.long ?: 0.0
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }

    data class Game(val data: GameData) : MapListItem() {
        override val id = data.id ?: ""
        override val lat = data.field?.lat ?: 0.0
        override val lng = data.field?.long ?: 0.0
        override val title = "🏀 ${data.field?.name ?: ""}"
        override val address = com.beballer.beballer.utils.BindingUtils.getGameSubtitle(data)
    }

    data class Ticket(val data: MapCourt) : MapListItem() {
        override val id = data.id ?: ""
        override val lat = data.lat ?: 0.0
        override val lng = data.long ?: 0.0
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }

    data class Tournament(val data: TournamentsEvent) : MapListItem() {
        override val id = data.id ?: ""
        override val lat = data.lat ?: 0.0
        override val lng = data.long ?: 0.0
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }

    data class Camp(val data: TournamentsEvent) : MapListItem() {
        override val id = data.id ?: ""
        override val lat = data.lat ?: 0.0
        override val lng = data.long ?: 0.0
        override val title = data.name ?: ""
        override val address = data.address ?: ""
    }
}

