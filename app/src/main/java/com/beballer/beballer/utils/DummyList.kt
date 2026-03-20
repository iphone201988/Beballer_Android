package com.beballer.beballer.utils

import com.beballer.beballer.data.model.GameModeModel
import com.beballer.beballer.data.model.GameModes

object DummyList {

    // add list game mode
    fun getListAccessibility(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Available to everyone"),
            GameModes("Available to licensees"),
            GameModes("Special opening hours")
        )
    }

    fun getListHoopsCount(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("1"),
            GameModes("2"),
            GameModes("3"),
            GameModes("4"),
            GameModes("5+"),

            )
    }

    // add list game mode
     fun getListBoardType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Steel"),
            GameModes("Wood"),
            GameModes("Plastic"),
            GameModes("Plexiglas"),
        )
    }

     fun getListNetType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("String"),
            GameModes("Chain"),
            GameModes("Plastic"),
            GameModes("No nets"),

            )
    }

     fun getListFloorType(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("Asphalt with gravel"),
            GameModes("Bitumen without gravel"),
            GameModes("synth"),
            GameModes("parquet"),

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