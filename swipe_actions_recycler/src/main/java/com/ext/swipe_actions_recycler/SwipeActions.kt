package com.ext.swipe_actions_recycler

import android.util.AttributeSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Main public API for SwipeActions library.
 *
 * Usage:
 * ```
 * val config = SwipeActionConfig.Builder(context)
 *     .rightActions(listOf(SwipeAction.delete(), SwipeAction.edit()))
 *     .callback(myCallback)
 *     .build()
 *
 * SwipeActions.attachTo(recyclerView, config)
 * ```
 */
object SwipeActions {

    private val attachedCallbacks = mutableMapOf<RecyclerView, SwipeActionsAttachment>()

    /**
     * Attachment holder for cleanup.
     */
    private data class SwipeActionsAttachment(
        val itemTouchHelper: ItemTouchHelper,
        val callback: SwipeTouchCallback
    )

    /**
     * Attach swipe actions to a RecyclerView.
     *
     * @param recyclerView The RecyclerView to attach to
     * @param config Configuration for swipe actions
     * @return ItemTouchHelper instance for advanced control (optional)
     */
    fun attachTo(recyclerView: RecyclerView, config: SwipeActionConfig): ItemTouchHelper {
        // Detach any existing attachment
        detachFrom(recyclerView)

        // Create callback and ItemTouchHelper
        val callback = SwipeTouchCallback(config, recyclerView)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Store for cleanup
        attachedCallbacks[recyclerView] = SwipeActionsAttachment(itemTouchHelper, callback)

        return itemTouchHelper
    }

    /**
     * Attach swipe actions using XML attributes from the RecyclerView.
     *
     * This allows configuration purely from XML:
     * ```xml
     * <androidx.recyclerview.widget.RecyclerView
     *     app:sar_button_width="80dp"
     *     app:sar_delete_button_color="@color/red"
     *     ... />
     * ```
     *
     * @param recyclerView The RecyclerView with XML attributes
     * @param attrs AttributeSet from XML
     * @param actions Actions to use (must be provided programmatically)
     * @param callback Callback for action events
     */
    fun attachTo(
        recyclerView: RecyclerView,
        attrs: AttributeSet?,
        leftActions: List<SwipeAction> = emptyList(),
        rightActions: List<SwipeAction> = emptyList(),
        callback: SwipeActionCallback?
    ): ItemTouchHelper {
        val config = SwipeActionConfig.Builder(recyclerView.context)
            .fromAttributes(attrs)
            .leftActions(leftActions)
            .rightActions(rightActions)
            .callback(callback)
            .build()

        return attachTo(recyclerView, config)
    }

    /**
     * Detach swipe actions from a RecyclerView.
     *
     * @param recyclerView The RecyclerView to detach from
     */
    fun detachFrom(recyclerView: RecyclerView) {
        val attachment = attachedCallbacks.remove(recyclerView) ?: return

        // Clean up
        attachment.callback.detach()
        attachment.itemTouchHelper.attachToRecyclerView(null)
    }

    /**
     * Close any currently open swipe on the RecyclerView.
     *
     * @param recyclerView The RecyclerView
     */
    fun closeOpenSwipe(recyclerView: RecyclerView) {
        attachedCallbacks[recyclerView]?.callback?.closeOpenSwipe()
    }

    /**
     * Get the currently swiped position in the RecyclerView.
     *
     * @param recyclerView The RecyclerView
     * @return Position of the swiped item, or RecyclerView.NO_POSITION if none
     */
    fun getCurrentSwipedPosition(recyclerView: RecyclerView): Int {
        return attachedCallbacks[recyclerView]?.callback?.getCurrentSwipedPosition()
            ?: RecyclerView.NO_POSITION
    }

    /**
     * Check if swipe actions are attached to a RecyclerView.
     *
     * @param recyclerView The RecyclerView
     * @return true if attached, false otherwise
     */
    fun isAttached(recyclerView: RecyclerView): Boolean {
        return attachedCallbacks.containsKey(recyclerView)
    }
}

/**
 * Extension function for convenient attachment.
 *
 * Usage:
 * ```
 * recyclerView.enableSwipeActions(config)
 * ```
 */
fun RecyclerView.enableSwipeActions(config: SwipeActionConfig): ItemTouchHelper {
    return SwipeActions.attachTo(this, config)
}

/**
 * Extension function to disable swipe actions.
 */
fun RecyclerView.disableSwipeActions() {
    SwipeActions.detachFrom(this)
}

/**
 * Extension function to close any open swipe.
 */
fun RecyclerView.closeOpenSwipe() {
    SwipeActions.closeOpenSwipe(this)
}