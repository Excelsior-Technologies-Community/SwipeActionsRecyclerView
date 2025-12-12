package com.ext.swipe_actions_recycler

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * Represents a single swipe action (e.g., Delete, Edit, Archive).
 *
 * @param id Unique identifier for this action (e.g., "delete", "edit")
 * @param text Display text for the button (optional, can show icon-only)
 * @param iconRes Drawable resource ID for the icon
 * @param backgroundColor Background color for this action button
 * @param textColor Text color (defaults to white)
 * @param iconTint Icon tint color (optional, null means no tint)
 * @param contentDescription Accessibility description
 * @param tag Optional tag for carrying custom data
 */
data class SwipeAction(
    val id: String,
    val text: String? = null,
    @DrawableRes val iconRes: Int? = null,
    @ColorInt val backgroundColor: Int,
    @ColorInt val textColor: Int = 0xFFFFFFFF.toInt(),
    @ColorInt val iconTint: Int? = null,
    val contentDescription: String? = null,
    val tag: Any? = null
) {

    /**
     * Builder for creating SwipeAction instances with a fluent API.
     */
    class Builder(private val id: String) {
        private var text: String? = null
        private var iconRes: Int? = null
        private var backgroundColor: Int = 0xFFFF6B6B.toInt() // Default red
        private var textColor: Int = 0xFFFFFFFF.toInt() // Default white
        private var iconTint: Int? = null
        private var contentDescription: String? = null
        private var tag: Any? = null

        fun text(text: String?) = apply { this.text = text }
        fun icon(@DrawableRes iconRes: Int?) = apply { this.iconRes = iconRes }
        fun backgroundColor(@ColorInt color: Int) = apply { this.backgroundColor = color }
        fun textColor(@ColorInt color: Int) = apply { this.textColor = color }
        fun iconTint(@ColorInt tint: Int?) = apply { this.iconTint = tint }
        fun contentDescription(desc: String?) = apply { this.contentDescription = desc }
        fun tag(tag: Any?) = apply { this.tag = tag }

        fun build() = SwipeAction(
            id = id,
            text = text,
            iconRes = iconRes,
            backgroundColor = backgroundColor,
            textColor = textColor,
            iconTint = iconTint,
            contentDescription = contentDescription,
            tag = tag
        )
    }

    companion object {
        /**
         * Create a delete action with default styling.
         */
        fun delete(
            text: String = "Delete",
            @DrawableRes iconRes: Int = R.drawable.sar_default_delete_icon,
            @ColorInt backgroundColor: Int = 0xFFFF3B30.toInt()
        ) = Builder("delete")
            .text(text)
            .icon(iconRes)
            .backgroundColor(backgroundColor)
            .contentDescription("Delete item")
            .build()

        /**
         * Create an edit action with default styling.
         */
        fun edit(
            text: String = "Edit",
            @DrawableRes iconRes: Int = R.drawable.sar_default_edit_icon,
            @ColorInt backgroundColor: Int = 0xFF8E8E93.toInt()
        ) = Builder("edit")
            .text(text)
            .icon(iconRes)
            .backgroundColor(backgroundColor)
            .contentDescription("Edit item")
            .build()

        /**
         * Create an archive action with default styling.
         */
        fun archive(
            text: String = "Archive",
            @DrawableRes iconRes: Int = R.drawable.sar_default_archive_icon,
            @ColorInt backgroundColor: Int = 0xFFFF9500.toInt()
        ) = Builder("archive")
            .text(text)
            .icon(iconRes)
            .backgroundColor(backgroundColor)
            .contentDescription("Archive item")
            .build()
    }
}

/**
 * Represents a set of actions for one swipe direction.
 *
 * @param actions List of actions to display
 * @param direction Swipe direction (LEFT or RIGHT)
 */
data class SwipeActionSet(
    val actions: List<SwipeAction>,
    val direction: SwipeDirection
)

/**
 * Swipe direction enumeration.
 */
enum class SwipeDirection {
    LEFT,
    RIGHT
}