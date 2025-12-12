package com.ext.swipe_actions_recycler

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * ItemTouchHelper.Callback with proper state management to prevent overlapping.
 */
class SwipeTouchCallback(
    private val config: SwipeActionConfig,
    private val recyclerView: RecyclerView
) : ItemTouchHelper.Callback() {

    private var currentSwipedPosition: Int = RecyclerView.NO_POSITION
    private var currentSwipeDirection: SwipeDirection? = null
    private var buttonDrawer: SwipeButtonDrawer? = null

    // Track if we're currently swiping to prevent overlaps
    private var isSwipeInProgress = false

    private val itemDecoration = SwipeItemDecoration()

    private val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if tapping on revealed buttons
                    if (currentSwipedPosition != RecyclerView.NO_POSITION) {
                        val drawer = buttonDrawer ?: return false
                        val hitResult = drawer.hitTest(e.x, e.y)

                        if (hitResult != null) {
                            // Tapped on button - consume event
                            return true
                        } else {
                            // Tapped outside buttons - close swipe
                            val viewHolder =
                                rv.findViewHolderForAdapterPosition(currentSwipedPosition)
                            viewHolder?.itemView?.let { closeSwipe(it) }
                            return true
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (currentSwipedPosition != RecyclerView.NO_POSITION) {
                        val drawer = buttonDrawer ?: return false
                        val hitResult = drawer.hitTest(e.x, e.y)

                        if (hitResult != null) {
                            val (action, direction) = hitResult
                            handleActionClicked(currentSwipedPosition, action, direction)
                            return true
                        }
                    }
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    init {
        recyclerView.addOnItemTouchListener(touchListener)
        recyclerView.addItemDecoration(itemDecoration)
    }

    fun detach() {
        recyclerView.removeOnItemTouchListener(touchListener)
        recyclerView.removeItemDecoration(itemDecoration)

        // Close any open swipe on detach
        if (currentSwipedPosition != RecyclerView.NO_POSITION) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(currentSwipedPosition)
            viewHolder?.itemView?.let { itemView ->
                itemView.translationX = 0f
                itemView.alpha = 1f
                itemView.elevation = 0f
            }
        }
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (!config.swipeEnabled) {
            return makeMovementFlags(0, 0)
        }

        // Don't allow new swipes if one is already open
        if (currentSwipedPosition != RecyclerView.NO_POSITION &&
            currentSwipedPosition != viewHolder.adapterPosition
        ) {
            return makeMovementFlags(0, 0)
        }

        var swipeFlags = 0

        if (config.leftActions.isNotEmpty()) {
            swipeFlags = swipeFlags or ItemTouchHelper.RIGHT
        }

        if (config.rightActions.isNotEmpty()) {
            swipeFlags = swipeFlags or ItemTouchHelper.LEFT
        }

        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used - we handle everything in onChildDraw
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return config.swipeThreshold
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return defaultValue * config.swipeVelocity
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Float.MAX_VALUE // Prevent item from being swiped away completely
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        val itemView = viewHolder.itemView
        val position = viewHolder.adapterPosition

        isSwipeInProgress = false

        // If there's some translation, snap to open or closed
        if (abs(itemView.translationX) > 0) {
            val swipeDirection = when {
                itemView.translationX > 0 -> SwipeDirection.LEFT
                itemView.translationX < 0 -> SwipeDirection.RIGHT
                else -> null
            }

            val maxReveal = getMaxRevealDistance(swipeDirection)
            val threshold = maxReveal * config.swipeThreshold
            val longThreshold = maxReveal * config.longSwipeThreshold
            val absDx = abs(itemView.translationX)

            if (absDx >= threshold) {
                val actions = when (swipeDirection) {
                    SwipeDirection.LEFT -> config.leftActions
                    SwipeDirection.RIGHT -> config.rightActions
                    null -> emptyList()
                }

                if (config.longSwipeEnabled && absDx >= longThreshold && actions.size == 1) {
                    // Trigger long swipe (quick action)
                    val action = actions[0]

                    // Haptic feedback
                    if (config.hapticOnAction) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        } else {
                            itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }

                    // Close swipe
                    closeSwipe(itemView)

                    // Call callback
                    config.callback?.onActionClicked(position, action.id, itemView)

                    // Optional long swipe callback
                    config.callback?.onLongSwipeTriggered(position, swipeDirection!!, itemView)
                } else {
                    // Snap to open
                    snapToOpen(itemView, swipeDirection, maxReveal, position)
                }
            } else {
                // Snap to closed
                closeSwipe(itemView)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            viewHolder.itemView.translationX = 0f
            viewHolder.itemView.elevation = 0f
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val itemView = viewHolder.itemView
        val position = viewHolder.adapterPosition

        if (position < 0 || position >= (recyclerView.adapter?.itemCount ?: 0)) {
            return
        }

        // Mark swipe in progress
        if (isCurrentlyActive) {
            isSwipeInProgress = true

            // Close previous item if different
            if (currentSwipedPosition != RecyclerView.NO_POSITION &&
                currentSwipedPosition != position
            ) {
                val prevHolder =
                    recyclerView.findViewHolderForAdapterPosition(currentSwipedPosition)
                prevHolder?.itemView?.let { closeSwipe(it) }
            }
        }

        // Initialize drawer
        if (buttonDrawer == null) {
            buttonDrawer = SwipeButtonDrawer(config, itemView)
        }

        // Determine direction
        val swipeDirection = when {
            dX > 0 -> SwipeDirection.LEFT
            dX < 0 -> SwipeDirection.RIGHT
            else -> null
        }

        // Calculate max reveal
        val maxReveal = getMaxRevealDistance(swipeDirection)

        // Limit swipe distance
        val limitedDx = when {
            dX > 0 -> dX.coerceIn(0f, maxReveal)
            dX < 0 -> dX.coerceIn(-maxReveal, 0f)
            else -> 0f
        }

        // Apply elevation
        itemView.elevation = if (abs(limitedDx) > 0) config.itemElevationOnSwipe else 0f

        // Draw buttons only during active gesture
        if (isCurrentlyActive) {
            buttonDrawer?.draw(c, itemView, limitedDx, dY, isCurrentlyActive)
        }

        // Move the item
        itemView.translationX = limitedDx

        // Update current state
        if (isCurrentlyActive) {
            currentSwipeDirection = swipeDirection
            if (abs(limitedDx) > 0) {
                currentSwipedPosition = position
            }
        }
    }

    private fun getMaxRevealDistance(direction: SwipeDirection?): Float {
        val actions = when (direction) {
            SwipeDirection.LEFT -> config.leftActions
            SwipeDirection.RIGHT -> config.rightActions
            null -> return 0f
        }

        if (actions.isEmpty()) return 0f

        // Calculate total width needed
        val buttonCount = actions.size.coerceAtMost(config.maxButtonsPerSide)
        return config.buttonWidth * buttonCount
    }

    private fun snapToOpen(
        itemView: View,
        direction: SwipeDirection?,
        maxReveal: Float,
        position: Int
    ) {
        currentSwipedPosition = position
        currentSwipeDirection = direction

        val targetX = when (direction) {
            SwipeDirection.LEFT -> maxReveal
            SwipeDirection.RIGHT -> -maxReveal
            null -> 0f
        }

        val animator = ValueAnimator.ofFloat(itemView.translationX, targetX)
        animator.duration = config.snapBackDuration.toLong()
        animator.addUpdateListener {
            itemView.translationX = it.animatedValue as Float
            recyclerView.invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isSwipeInProgress = false
            }
        })
        animator.start()
    }

    private fun closeSwipe(itemView: View) {
        currentSwipedPosition = RecyclerView.NO_POSITION
        currentSwipeDirection = null
        buttonDrawer?.buttonRects?.clear()

        val animator = ValueAnimator.ofFloat(itemView.translationX, 0f)
        animator.duration = config.snapBackDuration.toLong()
        animator.addUpdateListener {
            itemView.translationX = it.animatedValue as Float
            recyclerView.invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                itemView.elevation = 0f
                isSwipeInProgress = false
                buttonDrawer = null
            }
        })
        animator.start()
    }

    private fun handleActionClicked(position: Int, action: SwipeAction, direction: SwipeDirection) {
        if (position < 0 || position >= (recyclerView.adapter?.itemCount ?: 0)) {
            return
        }

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) ?: return
        val itemView = viewHolder.itemView

        // Haptic feedback
        if (config.hapticOnAction) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }

        // Close swipe
        closeSwipe(itemView)

        // Fire callback
        config.callback?.onActionClicked(position, action.id, itemView)
    }

    fun closeOpenSwipe() {
        if (currentSwipedPosition != RecyclerView.NO_POSITION) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(currentSwipedPosition)
            viewHolder?.itemView?.let { closeSwipe(it) }
        }
    }

    fun getCurrentSwipedPosition(): Int = currentSwipedPosition

    private inner class SwipeItemDecoration : RecyclerView.ItemDecoration() {
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            if (currentSwipedPosition == RecyclerView.NO_POSITION) return

            val vh = parent.findViewHolderForAdapterPosition(currentSwipedPosition) ?: return
            val itemView = vh.itemView
            val dX = itemView.translationX
            if (abs(dX) == 0f) return

            if (buttonDrawer == null) {
                buttonDrawer = SwipeButtonDrawer(config, itemView)
            }

            buttonDrawer?.draw(c, itemView, dX, 0f, false)
        }
    }
}