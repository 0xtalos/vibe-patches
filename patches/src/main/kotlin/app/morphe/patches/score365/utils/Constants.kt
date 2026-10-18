package app.morphe.patches.score365.utils

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/** 365Score application package name. */
const val PACKAGE_365SCORE = "com.scores365"

/**
 * Compatibility declaration for the 365Score app.
 * Add supported versions here as they are validated.
 */
val COMPATIBILITY_365SCORE = arrayOf(
    Compatibility(
        name = "365Scores",
        packageName = PACKAGE_365SCORE,
        targets = listOf(AppTarget("14.9.5")),
    ),
)
