package com.ext.swipe_actions_recycler

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes

/**
 * Configuration holder for SwipeActions library.
 *
 * Reads defaults from XML attributes and allows programmatic overrides.
 * Use Builder to create instances.
 */
data class SwipeActionConfig(
    // Appearance
    val buttonTextSize: Float,
    @ColorInt val buttonTextColor: Int,
    @ColorInt val buttonIconTint: Int?,
    val buttonCornerRadius: Float,
    val buttonPadding: Float,
    val buttonWidth: Float,
    val buttonIconSize: Float,
    val iconTextSpacing: Float,

    // Colors
    @ColorInt val deleteButtonColor: Int,
    @ColorInt val editButtonColor: Int,
    @ColorInt val archiveButtonColor: Int,
    @ColorInt val pinButtonColor: Int,
    @ColorInt val overlayColor: Int?,

    // Behavior
    val swipeThreshold: Float,
    val swipeVelocity: Float,
    val longSwipeEnabled: Boolean,
    val longSwipeThreshold: Float,
    val hapticOnAction: Boolean,
    val autoRemoveOnDelete: Boolean,
    val allowBothDirections: Boolean,
    val closePreviousOnSwipe: Boolean,
    val maxButtonsPerSide: Int,

    // Animation
    val revealAnimationDuration: Int,
    val snapBackDuration: Int,
    val slideAwayDuration: Int,
    val animationInterpolator: AnimationInterpolator,

    // General
    val itemElevationOnSwipe: Float,
    val swipeEnabled: Boolean,
    val minTouchTargetSize: Float,
    val showRippleOnTap: Boolean,
    val allowPartialSwipe: Boolean,

    // Actions
    val leftActions: List<SwipeAction>,
    val rightActions: List<SwipeAction>,

    // Callback
    val callback: SwipeActionCallback?
) {

    enum class AnimationInterpolator {
        LINEAR,
        ACCELERATE,
        DECELERATE,
        ACCELERATE_DECELERATE,
        OVERSHOOT
    }

    /**
     * Builder for SwipeActionConfig with fluent API.
     */
    class Builder(private val context: Context) {
        // Defaults
        private var buttonTextSize: Float = sp(14f)
        private var buttonTextColor: Int = 0xFFFFFFFF.toInt()
        private var buttonIconTint: Int? = 0xFFFFFFFF.toInt()
        private var buttonCornerRadius: Float = dp(8f)
        private var buttonPadding: Float = dp(16f)
        private var buttonWidth: Float = dp(80f)
        private var buttonIconSize: Float = dp(24f)
        private var iconTextSpacing: Float = dp(8f)

        private var deleteButtonColor: Int = 0xFFFF3B30.toInt()
        private var editButtonColor: Int = 0xFF8E8E93.toInt()
        private var archiveButtonColor: Int = 0xFFFF9500.toInt()
        private var pinButtonColor: Int = 0xFF007AFF.toInt()
        private var overlayColor: Int? = null

        private var swipeThreshold: Float = 0.3f
        private var swipeVelocity: Float = 1.0f
        private var longSwipeEnabled: Boolean = true
        private var longSwipeThreshold: Float = 0.7f
        private var hapticOnAction: Boolean = true
        private var autoRemoveOnDelete: Boolean = false
        private var allowBothDirections: Boolean = false
        private var closePreviousOnSwipe: Boolean = true
        private var maxButtonsPerSide: Int = 3

        private var revealAnimationDuration: Int = 250
        private var snapBackDuration: Int = 200
        private var slideAwayDuration: Int = 300
        private var animationInterpolator: AnimationInterpolator = AnimationInterpolator.DECELERATE

        private var itemElevationOnSwipe: Float = dp(4f)
        private var swipeEnabled: Boolean = true
        private var minTouchTargetSize: Float = dp(48f)
        private var showRippleOnTap: Boolean = true
        private var allowPartialSwipe: Boolean = true

        private var leftActions: List<SwipeAction> = emptyList()
        private var rightActions: List<SwipeAction> = emptyList()
        private var callback: SwipeActionCallback? = null

        // Utility functions for dp and sp conversion
        private fun dp(value: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics
        )

        private fun sp(value: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics
        )

        /**
         * Load configuration from XML attributes.
         */
        @SuppressLint("ResourceType")
        fun fromAttributes(attrs: AttributeSet?, @StyleableRes defStyleAttr: Int = 0): Builder {
            if (attrs == null) return this

            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.SwipeActionsRecyclerView,
                defStyleAttr,
                0
            )

            try {
                // Appearance
                buttonTextSize = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_button_textSize,
                    buttonTextSize
                )
                buttonTextColor = typedArray.getColor(
                    R.styleable.SwipeActionsRecyclerView_sar_button_textColor,
                    buttonTextColor
                )
                if (typedArray.hasValue(R.styleable.SwipeActionsRecyclerView_sar_button_iconTint)) {
                    buttonIconTint = typedArray.getColor(
                        R.styleable.SwipeActionsRecyclerView_sar_button_iconTint,
                        buttonIconTint ?: 0xFFFFFFFF.toInt()
                    )
                }
                buttonCornerRadius = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_button_cornerRadius,
                    buttonCornerRadius
                )
                buttonPadding = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_button_padding,
                    buttonPadding
                )
                buttonWidth = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_button_width,
                    buttonWidth
                )
                buttonIconSize = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_button_iconSize,
                    buttonIconSize
                )
                iconTextSpacing = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_button_iconTextSpacing,
                    iconTextSpacing
                )

                // Colors
                deleteButtonColor = typedArray.getColor(
                    R.styleable.SwipeActionsRecyclerView_sar_delete_button_color,
                    deleteButtonColor
                )
                editButtonColor = typedArray.getColor(
                    R.styleable.SwipeActionsRecyclerView_sar_edit_button_color,
                    editButtonColor
                )
                archiveButtonColor = typedArray.getColor(
                    R.styleable.SwipeActionsRecyclerView_sar_archive_button_color,
                    archiveButtonColor
                )
                pinButtonColor = typedArray.getColor(
                    R.styleable.SwipeActionsRecyclerView_sar_pin_button_color,
                    pinButtonColor
                )
                if (typedArray.hasValue(R.styleable.SwipeActionsRecyclerView_sar_overlay_color)) {
                    overlayColor = typedArray.getColor(
                        R.styleable.SwipeActionsRecyclerView_sar_overlay_color,
                        0
                    )
                }

                // Behavior
                swipeThreshold = typedArray.getFloat(
                    R.styleable.SwipeActionsRecyclerView_sar_swipe_threshold,
                    swipeThreshold
                )
                swipeVelocity = typedArray.getFloat(
                    R.styleable.SwipeActionsRecyclerView_sar_swipe_velocity,
                    swipeVelocity
                )
                longSwipeEnabled = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_longSwipe_enabled,
                    longSwipeEnabled
                )
                longSwipeThreshold = typedArray.getFloat(
                    R.styleable.SwipeActionsRecyclerView_sar_longSwipe_threshold,
                    longSwipeThreshold
                )
                hapticOnAction = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_haptic_on_action,
                    hapticOnAction
                )
                autoRemoveOnDelete = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_auto_remove_on_delete,
                    autoRemoveOnDelete
                )
                allowBothDirections = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_allow_both_directions,
                    allowBothDirections
                )
                closePreviousOnSwipe = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_close_previous_on_swipe,
                    closePreviousOnSwipe
                )
                maxButtonsPerSide = typedArray.getInt(
                    R.styleable.SwipeActionsRecyclerView_sar_max_buttons_per_side,
                    maxButtonsPerSide
                )

                // Animation
                revealAnimationDuration = typedArray.getInt(
                    R.styleable.SwipeActionsRecyclerView_sar_reveal_animation_duration,
                    revealAnimationDuration
                )
                snapBackDuration = typedArray.getInt(
                    R.styleable.SwipeActionsRecyclerView_sar_snap_back_duration,
                    snapBackDuration
                )
                slideAwayDuration = typedArray.getInt(
                    R.styleable.SwipeActionsRecyclerView_sar_slide_away_duration,
                    slideAwayDuration
                )
                val interpolatorValue = typedArray.getInt(
                    R.styleable.SwipeActionsRecyclerView_sar_animation_interpolator,
                    2 // Default to DECELERATE
                )
                animationInterpolator = AnimationInterpolator.values()[interpolatorValue]

                // General
                itemElevationOnSwipe = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_item_elevation_on_swipe,
                    itemElevationOnSwipe
                )
                swipeEnabled = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_swipe_enabled,
                    swipeEnabled
                )
                minTouchTargetSize = typedArray.getDimension(
                    R.styleable.SwipeActionsRecyclerView_sar_min_touch_target_size,
                    minTouchTargetSize
                )
                showRippleOnTap = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_show_ripple_on_tap,
                    showRippleOnTap
                )
                allowPartialSwipe = typedArray.getBoolean(
                    R.styleable.SwipeActionsRecyclerView_sar_allow_partial_swipe,
                    allowPartialSwipe
                )

            } finally {
                typedArray.recycle()
            }

            return this
        }

        // Fluent setters
        fun buttonTextSize(size: Float) = apply { this.buttonTextSize = size }
        fun buttonTextColor(@ColorInt color: Int) = apply { this.buttonTextColor = color }
        fun buttonIconTint(@ColorInt tint: Int?) = apply { this.buttonIconTint = tint }
        fun buttonCornerRadius(radius: Float) = apply { this.buttonCornerRadius = radius }
        fun buttonPadding(padding: Float) = apply { this.buttonPadding = padding }
        fun buttonWidth(width: Float) = apply { this.buttonWidth = width }
        fun buttonIconSize(size: Float) = apply { this.buttonIconSize = size }
        fun iconTextSpacing(spacing: Float) = apply { this.iconTextSpacing = spacing }

        fun deleteButtonColor(@ColorInt color: Int) = apply { this.deleteButtonColor = color }
        fun editButtonColor(@ColorInt color: Int) = apply { this.editButtonColor = color }
        fun archiveButtonColor(@ColorInt color: Int) = apply { this.archiveButtonColor = color }
        fun pinButtonColor(@ColorInt color: Int) = apply { this.pinButtonColor = color }
        fun overlayColor(@ColorInt color: Int?) = apply { this.overlayColor = color }

        fun swipeThreshold(threshold: Float) = apply { this.swipeThreshold = threshold }
        fun swipeVelocity(velocity: Float) = apply { this.swipeVelocity = velocity }
        fun longSwipeEnabled(enabled: Boolean) = apply { this.longSwipeEnabled = enabled }
        fun longSwipeThreshold(threshold: Float) = apply { this.longSwipeThreshold = threshold }
        fun hapticOnAction(enabled: Boolean) = apply { this.hapticOnAction = enabled }
        fun autoRemoveOnDelete(enabled: Boolean) = apply { this.autoRemoveOnDelete = enabled }
        fun allowBothDirections(allow: Boolean) = apply { this.allowBothDirections = allow }
        fun closePreviousOnSwipe(close: Boolean) = apply { this.closePreviousOnSwipe = close }
        fun maxButtonsPerSide(max: Int) = apply { this.maxButtonsPerSide = max }

        fun revealAnimationDuration(duration: Int) =
            apply { this.revealAnimationDuration = duration }

        fun snapBackDuration(duration: Int) = apply { this.snapBackDuration = duration }
        fun slideAwayDuration(duration: Int) = apply { this.slideAwayDuration = duration }
        fun animationInterpolator(interpolator: AnimationInterpolator) =
            apply { this.animationInterpolator = interpolator }

        fun itemElevationOnSwipe(elevation: Float) = apply { this.itemElevationOnSwipe = elevation }
        fun swipeEnabled(enabled: Boolean) = apply { this.swipeEnabled = enabled }
        fun minTouchTargetSize(size: Float) = apply { this.minTouchTargetSize = size }
        fun showRippleOnTap(show: Boolean) = apply { this.showRippleOnTap = show }
        fun allowPartialSwipe(allow: Boolean) = apply { this.allowPartialSwipe = allow }

        fun leftActions(actions: List<SwipeAction>) = apply { this.leftActions = actions }
        fun rightActions(actions: List<SwipeAction>) = apply { this.rightActions = actions }
        fun callback(callback: SwipeActionCallback?) = apply { this.callback = callback }

        fun build() = SwipeActionConfig(
            buttonTextSize = buttonTextSize,
            buttonTextColor = buttonTextColor,
            buttonIconTint = buttonIconTint,
            buttonCornerRadius = buttonCornerRadius,
            buttonPadding = buttonPadding,
            buttonWidth = buttonWidth,
            buttonIconSize = buttonIconSize,
            iconTextSpacing = iconTextSpacing,
            deleteButtonColor = deleteButtonColor,
            editButtonColor = editButtonColor,
            archiveButtonColor = archiveButtonColor,
            pinButtonColor = pinButtonColor,
            overlayColor = overlayColor,
            swipeThreshold = swipeThreshold,
            swipeVelocity = swipeVelocity,
            longSwipeEnabled = longSwipeEnabled,
            longSwipeThreshold = longSwipeThreshold,
            hapticOnAction = hapticOnAction,
            autoRemoveOnDelete = autoRemoveOnDelete,
            allowBothDirections = allowBothDirections,
            closePreviousOnSwipe = closePreviousOnSwipe,
            maxButtonsPerSide = maxButtonsPerSide,
            revealAnimationDuration = revealAnimationDuration,
            snapBackDuration = snapBackDuration,
            slideAwayDuration = slideAwayDuration,
            animationInterpolator = animationInterpolator,
            itemElevationOnSwipe = itemElevationOnSwipe,
            swipeEnabled = swipeEnabled,
            minTouchTargetSize = minTouchTargetSize,
            showRippleOnTap = showRippleOnTap,
            allowPartialSwipe = allowPartialSwipe,
            leftActions = leftActions,
            rightActions = rightActions,
            callback = callback
        )
    }
}