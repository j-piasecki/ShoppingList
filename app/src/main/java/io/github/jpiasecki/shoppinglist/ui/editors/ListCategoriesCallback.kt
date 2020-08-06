package io.github.jpiasecki.shoppinglist.ui.editors

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R

class ListCategoriesCallback(private val adapter: ListCategoriesAdapter) : ItemTouchHelper.Callback() {

    private val paint = Paint()

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemRemoved(viewHolder.adapterPosition)
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
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val view = viewHolder.itemView
            val background = RectF(view.right.toFloat(), view.top.toFloat(), view.left.toFloat(), view.bottom.toFloat())
            val icon = getDeleteIcon(view.context)

            paint.color = Color.RED
            c.drawRect(background, paint)

            if (icon != null) {
                val height = view.bottom - view.top
                val iconSize = height / 3f
                val iconRect =
                    RectF(
                        if (dX < 0) view.right - iconSize * 2 else view.left + iconSize,
                        view.top + iconSize,
                        if (dX < 0) view.right - iconSize else view.left + iconSize * 2,
                        view.bottom - iconSize
                    )

                c.drawBitmap(icon, null, iconRect, paint)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun getDeleteIcon(context: Context): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_24) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}