package com.beballer.beballer.utils

import com.beballer.beballer.data.model.GameModeModel

object DummyList {

    // add list game mode
    fun getListAccessibility(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("Available to everyone"),
            GameModeModel("Available to licensees"),
            GameModeModel("Special opening hours"),
        )
    }

    fun getListHoopsCount(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("1"),
            GameModeModel("2"),
            GameModeModel("3"),
            GameModeModel("4"),
            GameModeModel("5+"),

            )
    }

    // add list game mode
     fun getListBoardType(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("Steel"),
            GameModeModel("Wood"),
            GameModeModel("Plastic"),
            GameModeModel("Plexiglas"),
        )
    }

     fun getListNetType(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("String"),
            GameModeModel("Chain"),
            GameModeModel("Plastic"),
            GameModeModel("No nets"),

            )
    }

     fun getListFloorType(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("Asphalt with gravel"),
            GameModeModel("Bitumen without gravel"),
            GameModeModel("synth"),
            GameModeModel("parquet"),

            )
    }

     fun getListLineType(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("Up to standards"),
            GameModeModel("Not up to standard"),

            )
    }


     fun getListWaterPointType(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("With"),
            GameModeModel("Without"),

            )
    }
}