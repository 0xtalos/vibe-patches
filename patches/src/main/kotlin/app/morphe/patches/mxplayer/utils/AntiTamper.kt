package app.morphe.patches.mxplayer.utils

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.util.returnEarly

/*
 * MX Player ships a native integrity / anti-tamper module, libmx-bh.so (namespace `bh_anti`,
 * class `bh_anti::ApkDetect`, `parseSignerHash(bh_zip::ZipFile&)`). It is driven from the
 * heavily obfuscated helper class Lcom/ddx/axx/axx/axx/BH;, which owns four natives:
 * `n`, `w`, `n1` and `t`.
 *
 * Startup wiring (all of it happens inside Loxb;->onCreate()V):
 *
 *   Loxb.onCreate()  ->  Loxb.s()   ->  BH.b(Loxb, 1)                  -> BH.n(...)
 *   Loxb.onCreate()  ->  this.s()   ->  App.s()  ->  BH.d(App)         -> BH.n(...)
 *                                                ->  BH.w(new String[]{Const.b})
 *
 * BH.b and BH.d hand the APK and an expected-content blob to BH.n. When the APK no longer
 * matches that expectation the native side aborts the process from a worker thread, which
 * surfaces as:
 *
 *     Abort message: 'terminating'
 *     libc++_shared.so (std::terminate)
 *     libc++_shared.so (std::__ndk1::thread::~thread())
 *     libmx-bh.so (...)
 *
 * Every working (dex-modifying) patch trips this check, so it has to be neutralised for the
 * other MX Player patches to be usable at all.
 *
 * BH.n / BH.w themselves are `native` and therefore have no implementation, so returnEarly()
 * on them does nothing. The callers are stubbed instead:
 *
 *   - Loxb.s() and App.s() do nothing except drive the anti-tamper, so stubbing them severs
 *     the entire Java -> native detection path (BH.b, BH.d and BH.w all in one).
 *   - BH.d / BH.b are stubbed as well in case a future build calls them from elsewhere.
 *
 * BH.n1 is deliberately left alone: it is called by ActivityScreen when opening a media URI
 * and returns a value that drives URL handling, so it is not part of the startup check.
 */

private object AppAntiTamperEntryFingerprint : Fingerprint(
    definingClass = "Lcom/mxtech/videoplayer/ad/App;",
    name = "s",
    returnType = "V",
)

private object LoxbAntiTamperEntryFingerprint : Fingerprint(
    definingClass = "Loxb;",
    name = "s",
    returnType = "V",
)

private object IntegrityDetectFingerprint : Fingerprint(
    definingClass = "Lcom/ddx/axx/axx/axx/BH;",
    name = "d",
    parameters = listOf("Lcom/mxtech/videoplayer/ad/App;"),
    returnType = "V",
)

private object IntegrityDetectStartupFingerprint : Fingerprint(
    definingClass = "Lcom/ddx/axx/axx/axx/BH;",
    name = "b",
    parameters = listOf("Loxb;", "I"),
    returnType = "V",
)

/**
 * Neutralises MX Player's native anti-tamper check.
 *
 * Used by [app.morphe.patches.mxplayer.misc.bypassIntegrityCheckPatch], which every other MX
 * Player patch declares as a dependency: the check aborts the process as soon as the dex has
 * been modified, which is true for *any* applied patch, so it is a hard prerequisite rather
 * than an optional extra.
 *
 * Stubbing is idempotent: applying `return-void` twice just yields two returns.
 */
context(BytecodePatchContext)
internal fun neutraliseMxIntegrityCheck() {
    AppAntiTamperEntryFingerprint.method.returnEarly()
    LoxbAntiTamperEntryFingerprint.method.returnEarly()
    IntegrityDetectFingerprint.method.returnEarly()
    IntegrityDetectStartupFingerprint.method.returnEarly()
}
