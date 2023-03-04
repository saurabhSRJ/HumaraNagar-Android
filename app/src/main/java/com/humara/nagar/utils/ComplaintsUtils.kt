package com.humara.nagar.utils


import android.content.Context
import com.humara.nagar.R


object ComplaintsUtils {

    enum class StateName(val currentState: String) {
        SENT("sent"),
        IN_PROGRESS("inprogress"),
        RESOLVED("resolved"),
        WITHDRAW("withdrawn");

        companion object {
            fun getName(stateName: StateName, context: Context): String {
                context.resources.apply {
                    return when (stateName) {
                        SENT -> this.getString(R.string.sent)
                        IN_PROGRESS -> this.getString(R.string.inprogress)
                        RESOLVED -> this.getString(R.string.resolved)
                        WITHDRAW -> this.getString(R.string.withdraw)
                    }
                }
            }
        }
    }

    enum class StateColor(val color: Int) {
        SENT(R.color.stroke_red),
        IN_PROGRESS(R.color.stroke_yellow),
        RESOLVED(R.color.stroke_green),
        WITHDRAW(R.color.stroke_red);
    }

    enum class StateDrawable(val categoryName: String) {
        GARBAGE_COLLECTION("Garbage Issue"),
        DRAINAGE_SYSTEM("Sewage Issue"),
        ROAD_MAINTENANCE("Road Issue"),
        WATER_SUPPLY("Water Issue");

        companion object {
            fun getDrawable(stateName: StateDrawable): Int {
                return when (stateName) {
                    GARBAGE_COLLECTION -> R.drawable.garbage_disposal
                    DRAINAGE_SYSTEM -> R.drawable.street_animal_control
                    ROAD_MAINTENANCE -> R.drawable.road_repair
                    WATER_SUPPLY -> R.drawable.garbage_disposal

                }
            }
        }
    }
}