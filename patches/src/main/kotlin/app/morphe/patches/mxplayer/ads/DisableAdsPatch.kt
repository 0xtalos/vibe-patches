package app.morphe.patches.mxplayer.ads

import app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch
import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

private object LocalAdManagerAFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/local/ad/a;",
    name = "a",
    returnType = "Z",
)

private object LocalAdManagerDFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/local/ad/a;",
    name = "d",
    returnType = "Z",
)

private object LocalAdManagerCompanionAFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/local/ad/a\$a;",
    name = "a",
    returnType = "Z",
)

private object LocalAdManagerCompanionDFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/local/ad/a\$a;",
    name = "d",
    returnType = "Z",
)

private object LocalExitAdEnableExitDialogFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/LocalOnlyExitAdTest;",
    name = "enableExitDialogAd",
    returnType = "Z",
)

private object LocalExitAdEnableInterstitialFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/LocalOnlyExitAdTest;",
    name = "enableInterstitialAd",
    returnType = "Z",
)

private object LocalExitAdExitWithoutAdFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/LocalOnlyExitAdTest;",
    name = "exitWithoutAd",
    returnType = "Z",
)

private object LocalExitAdIsEligibleFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/LocalOnlyExitAdTest;",
    name = "isEligible",
    returnType = "Z",
)

private object PlaybackAdIsSupportFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/InterstitialAfterPlaybackTest;",
    name = "isSupportScenariosAd",
    returnType = "Z",
)

private object PlaybackAdSupportFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/InterstitialAfterPlaybackTest;",
    name = "supportScenariosAd",
    returnType = "Z",
)

private object PlaybackAdIsEligibleFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/abtest/InterstitialAfterPlaybackTest;",
    name = "isEligible",
    returnType = "Z",
)

private object AppOpenAdManagerHFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/ad/appopen/AppOpenAdManager;",
    name = "h",
    returnType = "Z",
)

private object AppOpenAdManagerOFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/ad/appopen/AppOpenAdManager;",
    name = "o",
    returnType = "Z",
)

private object AppOpenAdManagerI3Fingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/ad/appopen/AppOpenAdManager;",
    name = "I3",
    returnType = "Z",
)

private object FeedIsShowAdFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/Feed;",
    name = "isShowAd",
    returnType = "Z",
)

private object TVChannelIsShowAdFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/next/livetv/TVChannel;",
    name = "isShowAd",
    returnType = "Z",
)

private object TVProgramIsShowAdFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/next/livetv/TVProgram;",
    name = "isShowAd",
    returnType = "Z",
)

private object ConfigBeanNavPromoFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/ConfigBean;",
    name = "openNavigationPromotion",
    returnType = "Z",
)

private object ConfigBeanNavMidPromoFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/online/model/bean/ConfigBean;",
    name = "openNavigationMiddlePromotion",
    returnType = "Z",
)

/**
 * Disables advertisements across MX Player:
 * - Local media list, media manager, and tray native banners
 * - Playback exit dialog and interstitial ads
 * - App-open / startup splash ads
 * - Feed and TV channel advertisements
 * - Bottom navigation promotional popups
 */
@Suppress("unused")
val disableMxAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Removes banner, native, interstitial, exit, and app-open advertisements from MX Player.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)
    dependsOn(bypassIntegrityCheckPatch)

    execute {

        val returnFalseTargets = listOf(
            LocalAdManagerAFingerprint,
            LocalAdManagerDFingerprint,
            LocalAdManagerCompanionAFingerprint,
            LocalAdManagerCompanionDFingerprint,
            LocalExitAdEnableExitDialogFingerprint,
            LocalExitAdEnableInterstitialFingerprint,
            LocalExitAdIsEligibleFingerprint,
            PlaybackAdIsSupportFingerprint,
            PlaybackAdSupportFingerprint,
            PlaybackAdIsEligibleFingerprint,
            AppOpenAdManagerHFingerprint,
            AppOpenAdManagerOFingerprint,
            AppOpenAdManagerI3Fingerprint,
            FeedIsShowAdFingerprint,
            TVChannelIsShowAdFingerprint,
            TVProgramIsShowAdFingerprint,
            ConfigBeanNavPromoFingerprint,
            ConfigBeanNavMidPromoFingerprint,
        )

        for (target in returnFalseTargets) {
            val method = target.methodOrNull ?: continue
            if (method.implementation != null) {
                method.returnEarly(false)
            }
        }

        // Force exitWithoutAd to return true so the player exits cleanly without showing ads
        LocalExitAdExitWithoutAdFingerprint.methodOrNull?.let { method ->
            if (method.implementation != null) {
                method.returnEarly(true)
            }
        }
    }
}
