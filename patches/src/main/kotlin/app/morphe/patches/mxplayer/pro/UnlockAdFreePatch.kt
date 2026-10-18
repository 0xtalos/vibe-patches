package app.morphe.patches.mxplayer.pro

import app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch
import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

private object ADFreeGroupIsEnableFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/ADFreeGroup;",
    name = "isEnable",
    returnType = "Z",
)

private object ADFreeGroupIsEligibleFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/ADFreeGroup;",
    name = "isEligible",
    returnType = "Z",
)

private object ADFreeGroup1IsEnableFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/ADFreeGroup\$1;",
    name = "isEnable",
    returnType = "Z",
)

private object ADFreeGroup2IsEnableFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/ADFreeGroup\$2;",
    name = "isEnable",
    returnType = "Z",
)

private object AdFreeStatusIsValidFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/next/adfree/model/AdFreeStatus;",
    name = "isAdFreeValid",
    returnType = "Z",
)

private object FeedIsAdFreeOpenFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/Feed;",
    name = "isAdFreeOpen",
    returnType = "Z",
)

private object ConfigBeanShouldShowAdFreeFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/ConfigBean;",
    name = "shouldShowAdFreeView",
    returnType = "Z",
)

private object BaseAdFreeNeedShowDialogFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/next/adfree/model/BaseAdFreeRespBean;",
    name = "needShowAdDialog",
    returnType = "Z",
)

private object BaseAdFreeCanSkipAdFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/next/adfree/model/BaseAdFreeRespBean;",
    name = "canSkipAd",
    returnType = "Z",
)

private object BaseAdFreeIsPurchasedFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/next/adfree/model/BaseAdFreeRespBean;",
    name = "isPurchased",
    returnType = "Z",
)

/**
 * Unlocks Ad-Free VIP status in MX Player:
 * - Forces AdFree status validation to return true
 * - Enables skipping online video ads
 * - Sets purchased status to true
 * - Hides subscription and purchase promotion dialogs
 */
@Suppress("unused")
val unlockAdFreePatch = bytecodePatch(
    name = "Unlock Ad-Free mode",
    description = "Unlocks Ad-Free VIP status and enables skipping online video ads in MX Player.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)
    dependsOn(bypassIntegrityCheckPatch)

    execute {

        val returnTrueTargets = listOf(
            ADFreeGroupIsEnableFingerprint,
            ADFreeGroupIsEligibleFingerprint,
            ADFreeGroup1IsEnableFingerprint,
            ADFreeGroup2IsEnableFingerprint,
            AdFreeStatusIsValidFingerprint,
            FeedIsAdFreeOpenFingerprint,
            BaseAdFreeCanSkipAdFingerprint,
            BaseAdFreeIsPurchasedFingerprint,
        )

        for (target in returnTrueTargets) {
            val method = target.methodOrNull ?: continue
            if (method.implementation != null) {
                method.returnEarly(true)
            }
        }

        val returnFalseTargets = listOf(
            ConfigBeanShouldShowAdFreeFingerprint,
            BaseAdFreeNeedShowDialogFingerprint,
        )

        for (target in returnFalseTargets) {
            val method = target.methodOrNull ?: continue
            if (method.implementation != null) {
                method.returnEarly(false)
            }
        }
    }
}
