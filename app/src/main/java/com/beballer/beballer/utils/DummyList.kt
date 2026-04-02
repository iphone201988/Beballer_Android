package com.beballer.beballer.utils

import com.beballer.beballer.data.model.GameModes

object DummyList {

    // add list game mode
    fun getListAccessibility(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Available to everyone", apiValue = "availableToEveryone"),
            GameModes("Available to licensees", apiValue = "availableToLicensees"),
            GameModes("Special opening hours", apiValue = "specialOpeningHours")
        )
    }

    fun getListHoopsCount(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("1", apiValue = "1"),
            GameModes("2", apiValue = "2"),
            GameModes("3", apiValue = "3"),
            GameModes("4", apiValue = "4"),
            GameModes("5+", apiValue = "5+"),
        )
    }

    // add list game mode
    fun getListBoardType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Steel", apiValue = "steel"),
            GameModes("Wood", apiValue = "wood"),
            GameModes("Plastic", apiValue = "plastic"),
            GameModes("Plexiglas", apiValue = "plexiglas"),
        )
    }

    fun getListNetType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("String", apiValue = "string"),
            GameModes("Chain", apiValue = "chains"),
            GameModes("Plastic", apiValue = "plastic"),
            GameModes("No nets", apiValue = "without"),
        )
    }

    fun getListFloorType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Asphalt with gravel", apiValue = "gravelBitumen"),
            GameModes("Bitumen without gravel", apiValue = "bitumen"),
            GameModes("Synth", apiValue = "synthetic"),
            GameModes("Parquet", apiValue = "woodenFloor"),
        )
    }

    fun getListLineType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Up to standards"),
            GameModes("Not up to standard"),
        )
    }


    fun getListWaterPointType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("With"),
            GameModes("Without"),
        )
    }
}
