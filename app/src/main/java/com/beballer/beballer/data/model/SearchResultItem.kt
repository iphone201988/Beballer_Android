package com.beballer.beballer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchResultItem(
    val id: String,
    val internalId: String,
    val title: String?,
    val subtitle: String?,
    val imageURL: String?,
    val lat: Double?,
    val lng: Double?
) : Parcelable

/**
 * Extension functions to map different API models to a single SearchResultItem
 */

fun MapCourt.toSearchResult(): SearchResultItem {
    return SearchResultItem(
        id = this.id ?: "",
        internalId = this._id ?: "",
        title = this.name,
        subtitle = this.address ?: this.city,
        imageURL = this.photos?.firstOrNull(),
        lat = this.lat,
        lng = this.long
    )
}

fun GameData.toSearchResult(): SearchResultItem {
    val gameTitle = "🏀 ${this.field?.name ?: "Game"}"
    val subtitleText = com.beballer.beballer.utils.BindingUtils.getGameSubtitle(this)

    return SearchResultItem(
        id = this.id ?: "",
        internalId = this._id ?: "",
        title = gameTitle,
        subtitle = subtitleText,
        imageURL = this.field?.photos?.firstOrNull() as? String,
        lat = this.field?.lat,
        lng = this.field?.long
    )
}

fun TournamentsEvent.toSearchResult(): SearchResultItem {

    val subtitleText = com.beballer.beballer.utils.BindingUtils.getTournamentSubtitle(this)

    return SearchResultItem(
        id = this.id ?: "",
        internalId = this._id ?: "",
        title = this.name ?:"",
        subtitle = subtitleText,
        imageURL = this.eventPhotos?.firstOrNull() as? String,
        lat = this.lat,
        lng = this.long
    )
}
