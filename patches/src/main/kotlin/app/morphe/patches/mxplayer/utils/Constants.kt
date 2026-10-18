package app.morphe.patches.mxplayer.utils

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/** MX Player application package name. */
const val PACKAGE_MXPLAYER = "com.mxtech.videoplayer.ad"

/**
 * Compatibility declaration for MX Player.
 * Supported versions: 3.3.1 (2001003541) and compatible releases.
 */
val COMPATIBILITY_MXPLAYER = arrayOf(
    Compatibility(
        name = "MX Player",
        packageName = PACKAGE_MXPLAYER,
        targets = listOf(AppTarget("3.3.1")),
    ),
)
