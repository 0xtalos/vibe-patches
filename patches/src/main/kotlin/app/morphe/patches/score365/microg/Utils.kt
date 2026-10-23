package app.morphe.patches.score365.microg

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

internal fun MutableMethod.returnEarly() {
    addInstructions(0, "return-void")
}

internal fun MutableMethod.returnEarly(value: Boolean) {
    val literal = if (value) "0x1" else "0x0"
    addInstructions(
        0,
        """
        const/4 v0, $literal
        return v0
        """.trimIndent(),
    )
}

internal fun MutableMethod.returnEarly(value: Int) {
    addInstructions(
        0,
        """
        const/16 v0, 0x$value
        return v0
        """.trimIndent(),
    )
}

internal fun MutableMethod.returnEarly(value: String) {
    addInstructions(
        0,
        """
        const-string v0, "$value"
        return-object v0
        """.trimIndent(),
    )
}
