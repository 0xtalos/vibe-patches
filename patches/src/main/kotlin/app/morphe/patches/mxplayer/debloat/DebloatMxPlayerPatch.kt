package app.morphe.patches.mxplayer.debloat

import app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch
import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patches.mxplayer.utils.hideViewById
import app.morphe.patches.mxplayer.utils.hideViewInRegister
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val CLEANUP_LAYOUT_CLASS = "Lcom/mxtech/mediamanager/view/MediaManagerCleanUpLayout;"
private const val DRAWER_BINDING_CLASS = "Lwj;"

private object MediaManagerActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/mediamanager/MediaManagerActivity;",
    name = "onCreate",
    returnType = "V",
)

private object NavigationDrawerContentBaseBindFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/drawerlayout/NavigationDrawerContentBase;",
    name = "b",
    returnType = "V",
)

private object NavigationDrawerContentLocalMusicFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/drawerlayout/view/NavigationDrawerContentLocal;",
    name = "f",
    returnType = "V",
)

private object BottomPromotionControllerBindFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/bottompromotion/BottomPromotionControllerLayout;",
    name = "a",
    returnType = "V",
)

/**
 * Hides optional MX Player features that are unrelated to local video playback.
 *
 * Each option only hides the feature's view (and its click listener target), it does not delete
 * any code. If an anchor no longer exists the patch fails loudly instead of silently doing nothing.
 */
@Suppress("unused")
val debloatMxPlayerPatch = bytecodePatch(
    name = "Debloat MX Player",
    description = "Hides the cleaner, file transfer, local network, local music and bottom " +
        "promotion features from MX Player.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)
    dependsOn(bypassIntegrityCheckPatch)

    val removeCleaner by booleanOption(
        "mx_debloat_cleaner",
        true,
        title = "Remove cleaner",
        description = "Hides the storage cleaner card in the media manager.",
    )
    val removeFileTransfer by booleanOption(
        "mx_debloat_file_transfer",
        true,
        title = "Remove file transfer",
        description = "Hides the file transfer entry of the navigation drawer.",
    )
    val removeLocalNetwork by booleanOption(
        "mx_debloat_local_network",
        true,
        title = "Remove local network",
        description = "Hides the local network streaming entry of the navigation drawer.",
    )
    val removeMusicRow by booleanOption(
        "mx_debloat_music_row",
        true,
        title = "Remove local music row",
        description = "Hides the local music entry of the navigation drawer.",
    )
    val removeBottomPromotions by booleanOption(
        "mx_debloat_promotions",
        true,
        title = "Remove bottom promotions",
        description = "Stops MX Player from showing bottom navigation promotion cards.",
    )

    execute {

        if (removeCleaner == true) {
            val method = MediaManagerActivityOnCreateFingerprint.method

            // The cleaner card is stored in the media manager view binding and last assigned
            // its click listener. Hiding the view right after keeps the register holding it alive.
            val cleanupLayoutFieldIndex = method.indexOfFirstInstructionOrThrow {
                val reference = getReference<FieldReference>()
                reference?.definingClass == DRAWER_BINDING_CLASS &&
                    reference.name == "c" &&
                    reference.type == CLEANUP_LAYOUT_CLASS
            }
            val cleanupLayoutRegister =
                method.getInstruction<OneRegisterInstruction>(cleanupLayoutFieldIndex).registerA
            val setOnClickListenerIndex =
                method.indexOfFirstInstructionOrThrow(cleanupLayoutFieldIndex) {
                    getReference<MethodReference>()?.name == "setOnClickListener"
                }

            method.hideViewInRegister(setOnClickListenerIndex + 1, cleanupLayoutRegister)
        }

        if (removeFileTransfer == true) {
            NavigationDrawerContentBaseBindFingerprint.method.hideViewById("ll_file_transfer")
        }

        if (removeLocalNetwork == true) {
            NavigationDrawerContentBaseBindFingerprint.method.hideViewById("ll_local_network")
        }

        if (removeMusicRow == true) {
            NavigationDrawerContentLocalMusicFingerprint.method.returnEarly()
        }

        if (removeBottomPromotions == true) {
            BottomPromotionControllerBindFingerprint.method.returnEarly()
        }
    }
}

