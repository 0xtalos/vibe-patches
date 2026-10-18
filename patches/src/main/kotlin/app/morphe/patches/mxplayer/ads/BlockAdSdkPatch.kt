package app.morphe.patches.mxplayer.ads

import app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch
import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

private object GmaAdLoaderLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/AdLoader;",
    name = "loadAd",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
)

private object GmaAdLoaderLoadAdManagerAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/AdLoader;",
    name = "loadAd",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;"),
)

private object GmaAdLoaderLoadAdsFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/AdLoader;",
    name = "loadAds",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdRequest;", "I"),
)

private object GmaBaseAdViewLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/BaseAdView;",
    name = "loadAd",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
)

private object GmaInterstitialLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
    name = "load",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/AdRequest;",
        "Lcom/google/android/gms/ads/interstitial/InterstitialAdLoadCallback;",
    ),
)

// Note: this build only ships the AdRequest overload of InterstitialAd.load, so there is no
// AdManagerAdRequest variant to stub here.

private object GmaAppOpenLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
    name = "load",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/AdRequest;",
        "Lcom/google/android/gms/ads/appopen/AppOpenAd\$AppOpenAdLoadCallback;",
    ),
)

private object GmaRewardedLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;",
    name = "load",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/AdRequest;",
        "Lcom/google/android/gms/ads/rewarded/RewardedAdLoadCallback;",
    ),
)

private object GmaRewardedLoadManagerFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;",
    name = "load",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;",
        "Lcom/google/android/gms/ads/rewarded/RewardedAdLoadCallback;",
    ),
)

private object GmaRewardedInterstitialLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAd;",
    name = "load",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/AdRequest;",
        "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAdLoadCallback;",
    ),
)

private object GmaRewardedInterstitialLoadManagerFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAd;",
    name = "load",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;",
        "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAdLoadCallback;",
    ),
)

private object ImaAdBreakHandlerLoadFingerprint : Fingerprint(
    definingClass = "Lcom/mxplay/interactivemedia/internal/core/ImaAdBreakHandler;",
    name = "loadAd",
    returnType = "V",
    parameters = listOf(
        "Lcom/google/ads/interactivemedia/v3/api/player/AdMediaInfo;",
        "Lcom/google/ads/interactivemedia/v3/api/AdPodInfo;",
    ),
)

/**
 * Stops ad SDKs from loading ads at their source:
 * - Google Mobile Ads banners, native ads, interstitials, app open, rewarded and
 *   rewarded interstitial formats
 * - MX Player's Interactive Media Ads (IMA) break handler
 *
 * The Google Mobile Ads SDK is intentionally still initialised so that callbacks depending on
 * initialisation do not hang; only the load calls are stubbed.
 */
@Suppress("unused")
val blockMxAdSdkPatch = bytecodePatch(
    name = "Block ad SDK loaders",
    description = "Prevents the Google Mobile Ads and Interactive Media Ads SDKs from loading " +
        "any ad format in MX Player.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)
    dependsOn(bypassIntegrityCheckPatch)

    execute {

        val adLoadTargets = listOf(
            GmaAdLoaderLoadAdFingerprint,
            GmaAdLoaderLoadAdManagerAdFingerprint,
            GmaAdLoaderLoadAdsFingerprint,
            GmaBaseAdViewLoadAdFingerprint,
            GmaInterstitialLoadFingerprint,
            GmaAppOpenLoadFingerprint,
            GmaRewardedLoadFingerprint,
            GmaRewardedLoadManagerFingerprint,
            GmaRewardedInterstitialLoadFingerprint,
            GmaRewardedInterstitialLoadManagerFingerprint,
            ImaAdBreakHandlerLoadFingerprint,
        )

        for (target in adLoadTargets) {
            target.method.returnEarly()
        }
    }
}
