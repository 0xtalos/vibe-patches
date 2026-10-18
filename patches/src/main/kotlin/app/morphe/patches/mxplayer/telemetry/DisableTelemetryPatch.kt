package app.morphe.patches.mxplayer.telemetry

import app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch
import app.morphe.patches.mxplayer.utils.COMPATIBILITY_MXPLAYER
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.p0Register
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private object MxRealtimeTrackerTrackFingerprint : Fingerprint(
    definingClass = "Lted;",
    name = "a",
    returnType = "V",
)

private object MxRealtimeTrackerSendFingerprint : Fingerprint(
    definingClass = "Lted\$c;",
    name = "a",
    returnType = "V",
)

private object FirebaseAnalyticsSetCurrentScreenFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/analytics/FirebaseAnalytics;",
    name = "setCurrentScreen",
    returnType = "V",
)

private object AppsFlyerLogEventFingerprint : Fingerprint(
    definingClass = "Lcom/appsflyer/internal/AFb1rSDK;",
    name = "logEvent",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "Ljava/util/Map;"),
)

private object AppsFlyerLogEventListenerFingerprint : Fingerprint(
    definingClass = "Lcom/appsflyer/internal/AFb1rSDK;",
    name = "logEvent",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Ljava/util/Map;",
        "Lcom/appsflyer/attribution/AppsFlyerRequestListener;",
    ),
)

private object AppsFlyerLogSessionFingerprint : Fingerprint(
    definingClass = "Lcom/appsflyer/internal/AFb1rSDK;",
    name = "logSession",
    returnType = "V",
)

private object AppsFlyerLogAdRevenueFingerprint : Fingerprint(
    definingClass = "Lcom/appsflyer/internal/AFb1rSDK;",
    name = "logAdRevenue",
    returnType = "V",
)

private object MxTerminationDetectorOnTaskRemovedFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/telemetry/lifecycle/MxTerminationDetectorService;",
    name = "onTaskRemoved",
    returnType = "V",
)

/**
 * Disables telemetry reporting in MX Player:
 * - MX Player's own realtime tracker (`MxRealtimeTracker`)
 * - Firebase Analytics screen tracking
 * - AppsFlyer event, session and ad revenue reporting
 * - The termination detector service's report (the service still stops itself)
 */
@Suppress("unused")
val disableMxTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Stops MX Player from reporting usage, screens, sessions and playback " +
        "events to its own tracker, Firebase Analytics and AppsFlyer.",
) {
    compatibleWith(*COMPATIBILITY_MXPLAYER)
    dependsOn(bypassIntegrityCheckPatch)

    execute {

        // MX Player's realtime tracker. Stubbing the entry point stops every event from
        // being queued, and stubbing the sender is a second safety net.
        MxRealtimeTrackerTrackFingerprint.method.returnEarly()
        MxRealtimeTrackerSendFingerprint.method.returnEarly()

        FirebaseAnalyticsSetCurrentScreenFingerprint.method.returnEarly()

        AppsFlyerLogEventFingerprint.method.returnEarly()
        AppsFlyerLogEventListenerFingerprint.method.returnEarly()
        AppsFlyerLogSessionFingerprint.method.returnEarly()
        AppsFlyerLogAdRevenueFingerprint.method.returnEarly()

        // Skip the termination report but keep stopping the service so it does not leak.
        val onTaskRemoved = MxTerminationDetectorOnTaskRemovedFingerprint.method
        val reportIndex = onTaskRemoved.indexOfFirstInstructionOrThrow {
            getReference<MethodReference>()?.let {
                it.definingClass == "Lqcd;" && it.name == "a"
            } == true
        }
        onTaskRemoved.addInstructions(
            reportIndex,
            """
                invoke-virtual {v${onTaskRemoved.p0Register}}, Landroid/app/Service;->stopSelf()V
                return-void
            """,
        )
    }
}
