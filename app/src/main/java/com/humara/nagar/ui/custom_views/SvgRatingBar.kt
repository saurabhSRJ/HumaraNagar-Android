package com.humara.nagar.ui.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Shader
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

@RequiresApi(Build.VERSION_CODES.M)
class SvgRatingBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) :
    AppCompatRatingBar(context, attrs) {

    private var sampleTile: Bitmap? = null
    private val roundedCorners = floatArrayOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f)
    private val roundRectShape = RoundRectShape(roundedCorners, null, null)

    init {
        progressDrawable = createTile(progressDrawable, false) as LayerDrawable
    }

    /**
     * Converts a drawable to a tiled version of itself. It will recursively
     * traverse layer and state list drawables.
     */

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun createTile(drawable: Drawable, clip: Boolean): Drawable =
        when {
            drawable is DrawableWrapper -> {
                val inner = DrawableCompat.wrap(drawable)
//                if (inner != null) {
//                    inner = createTile(inner, clip)
//                    drawable.WrappedDrawable = inner
//                }
//                drawable
                inner
            }
            drawable is LayerDrawable -> {
                val n = drawable.numberOfLayers
                val outDrawables = arrayOfNulls<Drawable>(n)
                for (i in 0 until n) {
                    val id = drawable.getId(i)
                    outDrawables[i] = createTile(drawable.getDrawable(i),
                        id == android.R.id.progress || id == android.R.id.secondaryProgress)
                }
                val newBg = LayerDrawable(outDrawables)
                for (i in 0 until n) {
                    newBg.setId(i, drawable.getId(i))
                }
                newBg
            }
            drawable is BitmapDrawable -> {
                val tileBitmap = drawable.bitmap
                if (sampleTile == null) {
                    sampleTile = tileBitmap
                }
                val bitmapShader = BitmapShader(tileBitmap, Shader.TileMode.REPEAT,
                    Shader.TileMode.CLAMP)
                val shapeDrawable = ShapeDrawable(roundRectShape).apply {
                    paint.shader = bitmapShader
                    paint.colorFilter = drawable.paint.colorFilter
                }
                if (clip) ClipDrawable(shapeDrawable, Gravity.START, ClipDrawable.HORIZONTAL)
                else shapeDrawable
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && drawable is VectorDrawable -> {
                createTile(getBitmapDrawableFromVectorDrawable(drawable), clip)
            }
            drawable is VectorDrawableCompat -> {
                // Pre-Lollipop support.
                createTile(getBitmapDrawableFromVectorDrawable(drawable), clip)
            }
            else -> drawable
        }

    private fun getBitmapDrawableFromVectorDrawable(drawable: Drawable): BitmapDrawable {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDrawable(resources, bitmap)
    }

    @Synchronized override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (sampleTile != null) {
            val width = sampleTile!!.width * numStars
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                measuredHeight)
        }
    }
}