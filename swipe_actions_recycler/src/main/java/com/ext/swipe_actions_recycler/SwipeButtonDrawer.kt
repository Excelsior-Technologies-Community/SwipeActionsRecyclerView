package com.ext.swipe_actions_recycler

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.abs

/**
 * WhatsApp-style button drawer with no gaps between buttons.
 */
class SwipeButtonDrawer(
    private val config: SwipeActionConfig,
    val itemView: View
) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = config.buttonTextSize
        color = config.buttonTextColor
        textAlign = Paint.Align.CENTER
    }

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        config.overlayColor?.let { color = it }
    }

    val buttonRects = mutableListOf<ButtonRect>()

    data class ButtonRect(
        val action: SwipeAction,
        val rect: RectF,
        val direction: SwipeDirection
    )

    fun draw(
        canvas: Canvas,
        itemView: View,
        dX: Float,
        dY: Float,
        isCurrentlyActive: Boolean
    ) {
        buttonRects.clear()

        val itemLeft = itemView.left.toFloat()
        val itemTop = itemView.top.toFloat()
        val itemRight = itemView.right.toFloat()
        val itemBottom = itemView.bottom.toFloat()
        val itemHeight = itemBottom - itemTop

        // Draw overlay if configured
        config.overlayColor?.let { color ->
            overlayPaint.color = color
            canvas.drawRect(itemLeft, itemTop, itemRight, itemBottom, overlayPaint)
        }

        val revealWidth = abs(dX)

        when {
            dX > 0 -> {
                // Swiping right - reveal left actions
                val actions = config.leftActions
                if (actions.isNotEmpty()) {
                    drawActionsOnLeft(
                        canvas, actions, SwipeDirection.LEFT,
                        itemLeft, itemTop, itemBottom, revealWidth, itemHeight
                    )
                }
            }

            dX < 0 -> {
                // Swiping left - reveal right actions
                val actions = config.rightActions
                if (actions.isNotEmpty()) {
                    drawActionsOnRight(
                        canvas, actions, SwipeDirection.RIGHT,
                        itemRight, itemTop, itemBottom, revealWidth, itemHeight
                    )
                }
            }
        }
    }

    private fun drawActionsOnLeft(
        canvas: Canvas?,
        actions: List<SwipeAction>,
        direction: SwipeDirection,
        itemLeft: Float,
        itemTop: Float,
        itemBottom: Float,
        revealWidth: Float,
        itemHeight: Float
    ) {
        val buttonCount = actions.size.coerceAtMost(config.maxButtonsPerSide)
        val buttonWidth = config.buttonWidth

        var currentX = itemLeft

        for (i in 0 until buttonCount) {
            val action = actions[i]

            val buttonRect = RectF(
                currentX,
                itemTop,
                currentX + buttonWidth,
                itemBottom
            )

            if (canvas != null) {
                drawButton(canvas, action, buttonRect)
            }
            buttonRects.add(ButtonRect(action, buttonRect, direction))

            currentX += buttonWidth
        }
    }

    private fun drawActionsOnRight(
        canvas: Canvas?,
        actions: List<SwipeAction>,
        direction: SwipeDirection,
        itemRight: Float,
        itemTop: Float,
        itemBottom: Float,
        revealWidth: Float,
        itemHeight: Float
    ) {
        val buttonCount = actions.size.coerceAtMost(config.maxButtonsPerSide)
        val buttonWidth = config.buttonWidth

        var currentX = itemRight

        // Draw from right to left
        for (i in 0 until buttonCount) {
            val action = actions[buttonCount - 1 - i]

            val buttonRect = RectF(
                currentX - buttonWidth,
                itemTop,
                currentX,
                itemBottom
            )

            if (canvas != null) {
                drawButton(canvas, action, buttonRect)
            }
            buttonRects.add(ButtonRect(action, buttonRect, direction))

            currentX -= buttonWidth
        }
    }

    private fun drawButton(
        canvas: Canvas,
        action: SwipeAction,
        rect: RectF
    ) {
        // Draw background (no rounded corners for WhatsApp style)
        backgroundPaint.color = action.backgroundColor
        canvas.drawRect(rect, backgroundPaint)

        val centerX = rect.centerX()
        val centerY = rect.centerY()

        // Get icon
        val icon = action.iconRes?.let { resId ->
            ContextCompat.getDrawable(itemView.context, resId)
        }

        if (icon != null && action.text != null) {
            // Draw icon and text stacked
            drawIconAndText(canvas, icon, action, centerX, centerY)
        } else if (icon != null) {
            // Icon only
            drawIconOnly(canvas, icon, action, centerX, centerY)
        } else if (action.text != null) {
            // Text only
            drawTextOnly(canvas, action.text, action.textColor, centerX, centerY)
        }
    }

    private fun drawIconAndText(
        canvas: Canvas,
        icon: Drawable,
        action: SwipeAction,
        centerX: Float,
        centerY: Float
    ) {
        val iconSize = config.buttonIconSize.toInt()
        val spacing = config.iconTextSpacing

        // Apply tint
        val tint = action.iconTint ?: config.buttonIconTint
        tint?.let { icon.setTint(it) }

        // Calculate vertical layout
        val totalHeight = iconSize + spacing + textPaint.textSize
        val startY = centerY - totalHeight / 2

        // Draw icon
        val iconTop = startY
        val iconLeft = centerX - iconSize / 2

        icon.setBounds(
            iconLeft.toInt(),
            iconTop.toInt(),
            (iconLeft + iconSize).toInt(),
            (iconTop + iconSize).toInt()
        )
        icon.draw(canvas)

        // Draw text
        val textY = startY + iconSize + spacing + textPaint.textSize * 0.75f
        textPaint.color = action.textColor
        canvas.drawText(action.text ?: "", centerX, textY, textPaint)
    }

    private fun drawIconOnly(
        canvas: Canvas,
        icon: Drawable,
        action: SwipeAction,
        centerX: Float,
        centerY: Float
    ) {
        val iconSize = config.buttonIconSize.toInt()

        val tint = action.iconTint ?: config.buttonIconTint
        tint?.let { icon.setTint(it) }

        icon.setBounds(
            (centerX - iconSize / 2).toInt(),
            (centerY - iconSize / 2).toInt(),
            (centerX + iconSize / 2).toInt(),
            (centerY + iconSize / 2).toInt()
        )
        icon.draw(canvas)
    }

    private fun drawTextOnly(
        canvas: Canvas,
        text: String,
        textColor: Int,
        centerX: Float,
        centerY: Float
    ) {
        textPaint.color = textColor
        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, centerX, textY, textPaint)
    }

    fun computeButtonRects(dX: Float) {
        buttonRects.clear()

        val itemLeft = itemView.left.toFloat()
        val itemTop = itemView.top.toFloat()
        val itemRight = itemView.right.toFloat()
        val itemBottom = itemView.bottom.toFloat()
        val itemHeight = itemBottom - itemTop

        val revealWidth = abs(dX)

        when {
            dX > 0 -> {
                val actions = config.leftActions
                if (actions.isNotEmpty()) {
                    drawActionsOnLeft(
                        null, actions, SwipeDirection.LEFT,
                        itemLeft, itemTop, itemBottom, revealWidth, itemHeight
                    )
                }
            }

            dX < 0 -> {
                val actions = config.rightActions
                if (actions.isNotEmpty()) {
                    drawActionsOnRight(
                        null, actions, SwipeDirection.RIGHT,
                        itemRight, itemTop, itemBottom, revealWidth, itemHeight
                    )
                }
            }
        }
    }

    fun hitTest(x: Float, y: Float): Pair<SwipeAction, SwipeDirection>? {
        if (buttonRects.isEmpty()) {
            computeButtonRects(itemView.translationX)
        }
        for (buttonRect in buttonRects) {
            if (buttonRect.rect.contains(x, y)) {
                return Pair(buttonRect.action, buttonRect.direction)
            }
        }
        return null
    }

    fun getTotalButtonWidth(actions: List<SwipeAction>): Float {
        if (actions.isEmpty()) return 0f
        val buttonCount = actions.size.coerceAtMost(config.maxButtonsPerSide)
        return config.buttonWidth * buttonCount
    }
}