package com.ext.swipeactionsrecyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ext.swipe_actions_recycler.*

/**
 * Demo showing how to use the swipe library.
 *
 * Users can:
 * 1. Define their own actions (left/right)
 * 2. Handle clicks with their own logic
 * 3. No automatic item removal - full control
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SampleAdapter
    private val items = mutableListOf<SampleItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize data
        populateSampleData()

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        adapter = SampleAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup swipe actions
        setupSwipeActions()
    }

    private fun populateSampleData() {
        items.clear()
        for (i in 1..20) {
            items.add(
                SampleItem(
                    id = i,
                    title = "Item $i",
                    description = "Swipe to see actions"
                )
            )
        }
    }

    /**
     * EXAMPLE: How users will configure swipe actions in their app.
     *
     * They can:
     * - Add any number of actions on left/right
     * - Define custom colors, icons, text
     * - Handle clicks with their own logic (no forced behavior)
     */
    private fun setupSwipeActions() {
        // STEP 1: Define actions for LEFT side (revealed by swiping RIGHT)
        val archiveAction = SwipeAction.Builder("archive")
            .text("Archive")
            .icon(R.drawable.sar_default_archive_icon)
            .backgroundColor(0xFFFF9500.toInt()) // Orange
            .textColor(0xFFFFFFFF.toInt())
            .iconTint(0xFFFFFFFF.toInt())
            .contentDescription("Archive item")
            .build()

        // STEP 2: Define actions for RIGHT side (revealed by swiping LEFT)
        val editAction = SwipeAction.Builder("edit")
            .text("Edit")
            .icon(R.drawable.sar_default_edit_icon)
            .backgroundColor(0xFF8E8E93.toInt()) // Gray
            .textColor(0xFFFFFFFF.toInt())
            .iconTint(0xFFFFFFFF.toInt())
            .contentDescription("Edit item")
            .build()

        val deleteAction = SwipeAction.Builder("delete")
            .text("Delete")
            .icon(R.drawable.sar_default_delete_icon)
            .backgroundColor(0xFFFF3B30.toInt()) // Red
            .textColor(0xFFFFFFFF.toInt())
            .iconTint(0xFFFFFFFF.toInt())
            .contentDescription("Delete item")
            .build()

        // STEP 3: Create configuration
        val config = SwipeActionConfig.Builder(this)
            // Add actions to left (swipe right to reveal)
            .leftActions(listOf(archiveAction))

            // Add actions to right (swipe left to reveal)
            .rightActions(listOf(editAction, deleteAction))

            // Configure behavior (WhatsApp-like)
            .buttonWidth(resources.getDimension(R.dimen.swipe_button_width))
            .swipeThreshold(0.1f) // 20% swipe to snap open
            .swipeEnabled(true)
            .hapticOnAction(true)
            .closePreviousOnSwipe(true)

            // Animation settings
            .snapBackDuration(100) // Fast snap
            .itemElevationOnSwipe(resources.getDimension(R.dimen.swipe_item_elevation))

            // STEP 4: Define callback - this is where YOU handle the click
            .callback(object : SimpleSwipeActionCallback() {
                override fun onActionClicked(position: Int, actionId: String, itemView: View) {
                    // Validate position
                    if (position < 0 || position >= items.size) return

                    // Handle click
                    val item = items[position]
                    Toast.makeText(
                        this@MainActivity,
                        "Clicked $actionId on ${item.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .build()

        // STEP 5: Attach to RecyclerView
        SwipeActions.attachTo(recyclerView, config)
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView.disableSwipeActions()
    }
}

/**
 * Sample data model.
 */
data class SampleItem(
    val id: Int,
    val title: String,
    val description: String
)

/**
 * Simple RecyclerView adapter.
 */
class SampleAdapter(
    private val items: List<SampleItem>
) : RecyclerView.Adapter<SampleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.titleText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sample, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
    }

    override fun getItemCount() = items.size
}