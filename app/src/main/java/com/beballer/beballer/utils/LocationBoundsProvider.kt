package com.beballer.beballer.utils

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.beballer.beballer.data.model.LocationScope
import com.beballer.beballer.data.model.MapBounds
import com.beballer.beballer.data.model.OptimizedCountry
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.cos

class LocationBoundsProvider(
    private val context: Context
) {

    private val geocoder = Geocoder(context, Locale.getDefault())
    private val earthRadius = 6_371_000.0

    // Cache
    private var cachedBounds: MutableMap<LocationScope, MapBounds> = mutableMapOf()
    private var lastLocation: Location? = null
    private val cacheThresholdMeters = 500f

    private var cachedCountries: Map<String, OptimizedCountry>? = null

    suspend fun fetchBounds(
        location: Location,
        scope: LocationScope
    ): MapBounds {

        lastLocation?.let { last ->
            if (last.distanceTo(location) < cacheThresholdMeters) {
                cachedBounds[scope]?.let { return it }
            }
        }

        val bounds = when (scope) {

            // -------------------------
            // COUNTRY
            // -------------------------
            LocationScope.COUNTRY -> {

                val iso2 = countryCode(location)

                if (iso2 != null) {
                    boundsFor(iso2)
                        ?: radiusBasedBounds(location, LocationScope.COUNTRY)
                } else {
                    radiusBasedBounds(location, LocationScope.COUNTRY)
                }
            }

            // -------------------------
            // REGION
            // -------------------------
            LocationScope.REGION -> {

                val iso2 = countryCode(location)
                val regionName = regionName(location)

                if (iso2 != null && regionName != null) {
                    regionBounds(regionName, iso2)
                        ?: radiusBasedBounds(location, LocationScope.REGION)
                } else {
                    radiusBasedBounds(location, LocationScope.REGION)
                }
            }

            // -------------------------
            // CITY
            // -------------------------
            LocationScope.CITY -> {
                radiusBasedBounds(location, LocationScope.CITY)
            }
        }

        lastLocation = location
        cachedBounds[scope] = bounds
        return bounds
    }

    // --------------------------------------------------
    // COUNTRY / REGION LOOKUP (From countries.json)
    // --------------------------------------------------

    suspend fun countryFor(iso2: String): OptimizedCountry? {
        if (cachedCountries == null) {
            cachedCountries = loadCountries()
        }
        return cachedCountries?.get(iso2)
    }

    suspend fun boundsFor(iso2: String): MapBounds? {
        val country = countryFor(iso2) ?: return null

        return MapBounds(
            country.bounds.maxLat,
            country.bounds.maxLng,
            country.bounds.minLat,
            country.bounds.minLng
        )
    }

    suspend fun regionBounds(
        regionName: String,
        iso2: String
    ): MapBounds? {

        val country = countryFor(iso2) ?: return null
        val region = country.regions[regionName] ?: return null

        return MapBounds(
            region.bounds.maxLat,
            region.bounds.maxLng,
            region.bounds.minLat,
            region.bounds.minLng
        )
    }

    // --------------------------------------------------
    // GEOCODER HELPERS (FREE)
    // --------------------------------------------------

    private suspend fun countryCode(location: Location): String? =
        withContext(Dispatchers.IO) {
            try {
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )?.firstOrNull()?.countryCode
            } catch (_: Exception) {
                null
            }
        }

    private suspend fun regionName(location: Location): String? =
        withContext(Dispatchers.IO) {
            try {
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )?.firstOrNull()?.adminArea
            } catch (_: Exception) {
                null
            }
        }

    // --------------------------------------------------
    // RADIUS LOGIC (Same Math as iOS)
    // --------------------------------------------------

    private fun radiusBasedBounds(
        location: Location,
        scope: LocationScope
    ): MapBounds {

        val radius = when (scope) {
            LocationScope.CITY -> 10_000.0
            LocationScope.REGION -> 80_000.0
            LocationScope.COUNTRY -> 600_000.0
        }

        val latRad = Math.toRadians(location.latitude)
        val lngRad = Math.toRadians(location.longitude)

        val latDelta = radius / earthRadius
        val lngDelta = radius / (earthRadius * cos(latRad))

        val northEastLat = Math.toDegrees(latRad + latDelta)
        val southWestLat = Math.toDegrees(latRad - latDelta)
        val northEastLng = Math.toDegrees(lngRad + lngDelta)
        val southWestLng = Math.toDegrees(lngRad - lngDelta)

        return MapBounds(
            clampLat(northEastLat),
            clampLng(northEastLng),
            clampLat(southWestLat),
            clampLng(southWestLng)
        )
    }

    private fun clampLat(value: Double) = value.coerceIn(-90.0, 90.0)
    private fun clampLng(value: Double) = value.coerceIn(-180.0, 180.0)

    // --------------------------------------------------
    // Load countries.json from assets (FREE)
    // --------------------------------------------------

    private fun loadCountries(): Map<String, OptimizedCountry> {
        return try {
            val inputStream = context.assets.open("countries.json")
            val json = inputStream.bufferedReader().use { it.readText() }

            val type = object : TypeToken<Map<String, OptimizedCountry>>() {}.type
            Gson().fromJson(json, type)

        } catch (_: Exception) {
            emptyMap()
        }
    }
}