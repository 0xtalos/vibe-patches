package app.morphe.patches.mxplayer.utils

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.util.getFreeRegisterProvider
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Class that holds MX Player's `R.id` constants.
 *
 * The resource id class is obfuscated, so it has to be matched by its descriptor.
 * If a future MX Player update renames it, the debloat patch fails loudly instead of
 * silently doing nothing.
 */
internal const val MXPLAYER_RESOURCES_CLASS = "Lkng;"

/** `View.GONE`. */
private const val VIEW_GONE = "0x8"

/** Owner of the set of Android views MX Player binds in a layout. */
private const val VIEW_CLASS = "Landroid/view/View;"

/**
 * Hides the view whose id is resolved by `sget <reg>, [MXPLAYER_RESOURCES_CLASS]->[resourceIdName]:I`.
 *
 * The view is obtained from the result register of the `findViewById`/view-finder call that
 * follows the id lookup, and is forced to [VIEW_GONE] after the app first changes its visibility.
 * Any later `setVisibility(0)` performed by MX Player is therefore overridden.
 *
 * @param resourceIdName Name of the `R.id` field, for example `ll_file_transfer`.
 * @throws PatchException If the id is not looked up, if the resulting view is never passed to
 *                        `View.setVisibility`, or if the view only lives in a high register.
 */
internal fun MutableMethod.hideViewById(resourceIdName: String) {
    val resourceIdIndex = indexOfFirstInstruction {
        opcode == Opcode.SGET && getReference<FieldReference>()?.let {
            it.definingClass == MXPLAYER_RESOURCES_CLASS && it.name == resourceIdName
        } == true
    }
    if (resourceIdIndex < 0) {
        throw PatchException(
            "Could not find MX Player resource id '$resourceIdName' in $this. " +
                "The resource class may have been renamed or the id was removed.",
        )
    }

    // The instruction right after the id lookup resolves the view.
    val viewLookupIndex = resourceIdIndex + 1
    val viewLookupReference = getInstruction(viewLookupIndex).getReference<MethodReference>()
    if (viewLookupReference == null ||
        (viewLookupReference.name != "findViewById" && viewLookupReference.name != "a")
    ) {
        throw PatchException(
            "Resource id '$resourceIdName' in $this is not resolved with a view lookup " +
                "(found ${viewLookupReference?.name}).",
        )
    }

    val moveResultIndex = viewLookupIndex + 1
    if (getInstruction(moveResultIndex).opcode != Opcode.MOVE_RESULT_OBJECT) {
        throw PatchException(
            "Resource id '$resourceIdName' in $this does not resolve a view object.",
        )
    }
    val viewRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

    val setVisibilityIndex = indexOfFirstInstruction(moveResultIndex) {
        val reference = getReference<MethodReference>()
        (this as? FiveRegisterInstruction)?.registerC == viewRegister &&
            reference?.definingClass == VIEW_CLASS &&
            reference.name == "setVisibility"
    }
    if (setVisibilityIndex < 0) {
        throw PatchException(
            "View for resource id '$resourceIdName' (v$viewRegister) is never passed to " +
                "View.setVisibility in $this, so it cannot be hidden.",
        )
    }

    hideViewInRegister(setVisibilityIndex + 1, viewRegister)
}

/**
 * Inserts `view.setVisibility(View.GONE)` before [insertIndex], guarded by a null check on
 * [viewRegister]. [viewRegister] must hold the view on every path that reaches [insertIndex].
 *
 * @param insertIndex Index the hide instructions are inserted at. The instruction currently at
 *                    this index becomes the target of the null-check skip branch.
 * @param viewRegister Register holding the view to hide. Must be a 4-bit register.
 * @throws PatchException If [viewRegister] is not a 4-bit register.
 */
internal fun MutableMethod.hideViewInRegister(insertIndex: Int, viewRegister: Int) {
    if (viewRegister >= 16) {
        throw PatchException(
            "Cannot hide a view in the high register v$viewRegister of $this; " +
                "the hide instructions require a 4-bit register.",
        )
    }

    val constantRegister = getFreeRegisterProvider(insertIndex, 1, viewRegister).getFreeRegister4Bit()
    val targetInstruction = getInstruction(insertIndex)

    addInstructionsWithLabels(
        insertIndex,
        """
            if-eqz v$viewRegister, :mx_hidden
            const/16 v$constantRegister, $VIEW_GONE
            invoke-virtual {v$viewRegister, v$constantRegister}, $VIEW_CLASS->setVisibility(I)V
        """,
        ExternalLabel("mx_hidden", targetInstruction),
    )
}
