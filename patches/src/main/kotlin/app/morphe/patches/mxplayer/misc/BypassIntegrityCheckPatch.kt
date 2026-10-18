package app.morphe.patches.mxplayer.misc

import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patches.mxplayer.utils.neutraliseMxIntegrityCheck
import app.morphe.patcher.patch.bytecodePatch

/**
 * Stubs MX Player's native anti-tamper entry points so the app does not abort when the dex has
 * been modified (which is always the case for a patched build).
 *
 * See [neutraliseMxIntegrityCheck] for the details of the check that is being bypassed.
 */
@Suppress("unused")
val bypassIntegrityCheckPatch = bytecodePatch(
    name = "Bypass integrity check",
    description = "Neutralises MX Player's native anti-tamper check (libmx-bh.so), which otherwise aborts the process with 'std::terminate' shortly after launch on any patched build.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)

    execute {
        neutraliseMxIntegrityCheck()
    }
}
