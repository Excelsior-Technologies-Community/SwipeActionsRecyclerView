package com.ext.swipe_actions_recycler

import android.view.View

/**
 * Callback interface for swipe action events.
 *
 * App implements this interface to respond to user interactions with swipe actions.
 */
interface SwipeActionCallback {

    /**
     * Called when a swipe action button is clicked.
     *
     * @param position Position of the item in the adapter
     * @param actionId Unique identifier of the action (e.g., "delete", "edit")
     * @param itemView The RecyclerView item view that was swiped
     */
    fun onActionClicked(position: Int, actionId: String, itemView: View)

    /**
     * Called when a swipe gesture starts on an item.
     *
     * @param position Position of the item being swiped
     * @param direction Direction of the swipe (LEFT or RIGHT)
     * @param itemView The RecyclerView item view
     */
    fun onSwipeStarted(position: Int, direction: SwipeDirection, itemView: View) {
        // Optional override
    }

    /**
     * Called when a swipe gesture is canceled (user releases before threshold).
     *
     * @param position Position of the item
     * @param itemView The RecyclerView item view
     */
    fun onSwipeCanceled(position: Int, itemView: View) {
        // Optional override
    }

    /**
     * Called when a swipe gesture completes and actions are revealed.
     *
     * @param position Position of the item
     * @param direction Direction of the swipe
     * @param itemView The RecyclerView item view
     */
    fun onSwipeCompleted(position: Int, direction: SwipeDirection, itemView: View) {
        // Optional override
    }

    /**
     * Called when a long-swipe triggers an immediate action.
     *
     * @param position Position of the item
     * @param direction Direction of the long-swipe
     * @param itemView The RecyclerView item view
     */
    fun onLongSwipeTriggered(position: Int, direction: SwipeDirection, itemView: View) {
        // Optional override
    }

    /**
     * Called before an item is removed (only if auto-remove is enabled).
     * Return false to prevent removal.
     *
     * @param position Position of the item to be removed
     * @return true to allow removal, false to cancel
     */
    fun onBeforeItemRemoved(position: Int): Boolean {
        return true
    }

    /**
     * Called after an item has been removed from the adapter.
     *
     * @param position Position where the item was removed
     */
    fun onItemRemoved(position: Int) {
        // Optional override
    }
}

/**
 * Simple adapter for SwipeActionCallback to avoid implementing all methods.
 */
abstract class SimpleSwipeActionCallback : SwipeActionCallback {
    override fun onActionClicked(position: Int, actionId: String, itemView: View) {
        // Must be implemented by subclass
    }
}