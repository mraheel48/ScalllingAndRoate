package com.example.scalllingandroate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat

class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var rootLayout: View
    var lastEvent: FloatArray? = null
    var d = 0f
    var newRot = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f
    var imageView: ImageView
    var rootLayoutSticker: FrameLayout
    var callBack: CustomImageCallBack? = null
    var exactBitmap: Bitmap? = null
    var flip = 0

    init {
        val mInflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootLayout = mInflater.inflate(R.layout.new_image_sticker, this, true)
        rootLayoutSticker = rootLayout.findViewById(R.id.root_layout_sticker)
        imageView = rootLayout.findViewById(R.id.imageViewSticker)
        this.setOnTouchListener(View.OnTouchListener { v, event ->
            viewTransformation(v, event)
            true
        })

        hideBorder()
    }

    fun setBitMap(bitmap: Bitmap?) {
        bitmap?.let {
            imageView.setImageBitmap(it)
            imageView.alpha = 0.30f
        }
    }

    fun hideBorder() {
        rootLayoutSticker.setBackgroundColor(Color.TRANSPARENT)
    }

    fun showBorder() {
        rootLayoutSticker.background =
            ContextCompat.getDrawable(context, R.drawable.shape_black_border)
    }

    fun setImagePath(imagePath: String) {
        exactBitmap = Util.scaleDown(Util.getBitmapOrg(imagePath), 1080f, true)
        if (exactBitmap != null) {
            this.imageView.alpha = 0.5f
            this.imageView.setImageBitmap(exactBitmap)
        }
    }

    fun updateCallBack(callBackClick: CustomImageCallBack) {
        callBack = callBackClick
    }

    fun deleteObject() {
        if (this@CustomImageView.parent != null) {
            val myCanvas = this@CustomImageView.parent as ViewGroup
            myCanvas.removeView(this@CustomImageView)
            this@CustomImageView.imageView.setImageDrawable(null)
            this@CustomImageView.imageView.setImageBitmap(null)
            this@CustomImageView.imageView.destroyDrawingCache()
            if (exactBitmap != null) {
                exactBitmap!!.recycle()
                exactBitmap = null
            }
            Util.clearGarbageCollection()
            callBack?.stickerViewDeleteClick()
        }
    }

    fun flipRoot() {
        if (flip % 2 == 0) {
            imageView.scaleX = -1f
            flip++
        } else {
            flip++
            imageView.scaleX = 1f
        }

    }

    fun disableAllOthers() {
        if (this@CustomImageView.parent != null) {
            val myViewGroup = this@CustomImageView.parent as ViewGroup
            for (i in 0 until myViewGroup.childCount) {
                if (myViewGroup.getChildAt(i) is CustomImageView) {
                    (myViewGroup.getChildAt(i) as CustomImageView).setControlItemsHidden(true)
                }
            }
        }
    }

    private fun setControlItemsHidden(isHidden: Boolean) {
        if (isHidden) {
            hideBorder()
        } else {
            showBorder()
        }
    }

    private fun viewTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {

                callBack?.stickerViewClickDown(this@CustomImageView)

                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY
                start.set(event.x, event.y)
                isOutSide = false
                mode = DRAG
                lastEvent = null

            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }
            MotionEvent.ACTION_UP -> {
                showBorder()
                isZoomAndRotate = false
                if (mode == DRAG) {
                    event.x
                    event.y
                }
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                setControlItemsHidden(false)
                if (mode == DRAG) {
                    isZoomAndRotate = false
                    view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate)
                        .setDuration(0).start()
                }
                if (mode == ZOOM && event.pointerCount == 2) {
                    val newDist1 = spacing(event)
                    if (newDist1 > 10f) {
                        val scale: Float = newDist1 / oldDist * view.scaleX
                        if (scale in 0.3..2.5) {
                            view.scaleX = scale
                            view.scaleY = scale
                            Log.d("mySticker", "${scale}")
                        }
                    }
                    if (lastEvent != null) {
                        newRot = rotation(event)
                        view.rotation = (view.rotation + (newRot - d))
                    }
                }
            }

        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    interface CustomImageCallBack {
        fun stickerViewClickDown(currentView: View?)
        fun stickerViewDeleteClick()
        fun stickerViewScrollViewEnable()
        fun stickerViewScrollViewDisable()
    }
}