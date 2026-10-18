package app.morphe.patches.mxplayer.misc

import app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch
import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

private object CheckForDnsBlockerFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/CheckForDnsBlocker;",
    name = "b",
    returnType = "V",
)

/**
 * Bypasses MX Player's anti-adblock / private DNS detection.
 * Stubs CheckForDnsBlocker.b() so the app does not trigger DNS blocker popups
 * or block playback when using AdGuard, NextDNS, Pi-hole, or VPN ad-blockers.
 */
@Suppress("unused")
val bypassDnsBlockerCheckPatch = bytecodePatch(
    name = "Bypass DNS blocker check",
    description = "Disables private DNS / ad-blocker detection so video playback works seamlessly with AdGuard, NextDNS, and Pi-hole.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)
    dependsOn(bypassIntegrityCheckPatch)

    execute {

        CheckForDnsBlockerFingerprint.methodOrNull?.let { method ->
            if (method.implementation != null) {
                method.returnEarly()
            }
        }
    }
}
