package uz.gita.facedetection.drawer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import uz.gita.facedetection.data.FaceData


class DrawFaceLine @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {
    private val isPortrait = context.resources.configuration.orientation == 1
    val rectPaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val circlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val roundRectPaintTransparent = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.FILL
    }

    val roundRectPaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 2f
    }

    private var faceData: FaceData? = null

    fun draw(values: FaceData) {
        faceData = values
        invalidate()
    }

    private fun drawCircleFace(canvas: Canvas?) {
        val faceLeft = if (isPortrait) {
            width * 0.166f
        } else {
            width * 0.33f
        }//left
        val faceTop = if (isPortrait) {
            height * 0.25f
        } else {
            height * 0.05f
        }//top
        val faceRight = if (isPortrait) {
            width * 0.833f
        } else {
            width * 0.66f
        }//right
        val faceBottom = if (isPortrait) {
            height * 0.75f
        } else {
            height * 0.95f
        }//bottom
        canvas?.drawRoundRect(
            faceLeft,
            faceTop,
            faceRight,
            faceBottom,
            height.toFloat(),
            height.toFloat(),
            roundRectPaintTransparent
        )
        canvas?.drawRoundRect(
            faceLeft,
            faceTop,
            faceRight,
            faceBottom,
            height.toFloat(),
            height.toFloat(),
            roundRectPaint
        )
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawCircleFace(canvas)
        drawFace(canvas, faceData)
    }


    var isFront = true
    var imageWidth: Int = width
    var imageHeight: Int = height
    private fun drawFace(canvas: Canvas?, faceData: FaceData?) {

        faceData?.let {
            var _left = it.rect.left
            val _right = it.rect.right
            val _bottom = it.rect.bottom * height / imageHeight
            val _top = it.rect.top * height / imageHeight
            it.faceLineData.forEach {
                paint.color = it.color
                canvas?.drawCircle(
                    it.startPoint.x.screenConverter(),
                    it.startPoint.y * height / imageHeight,
                    2f,
                    circlePaint
                )
                canvas?.drawCircle(
                    it.endPoint.x.screenConverter(),
                    it.endPoint.y * height / imageHeight,
                    2f,
                    circlePaint
                )
                canvas?.drawLine(
                    it.startPoint.x.screenConverter(),
                    it.startPoint.y * height / imageHeight,
                    it.endPoint.x.screenConverter(),
                    it.endPoint.y * height / imageHeight,
                    paint
                )
            }
            canvas?.drawRect(
                _left.screenConverter(),
                _top.toFloat(),
                _right.screenConverter(),
                _bottom.toFloat(),
                rectPaint
            )
        }
    }

    fun Int.screenConverter(): Float = if (isFront) {
        if (this < imageWidth) {
            ((imageWidth - this) * width / imageWidth).toFloat()
        } else {
            ((this - imageWidth) * width / imageWidth).toFloat()
        }
    } else {
        (this * width / imageWidth).toFloat()
    }

    fun Float.screenConverter(): Float = if (isFront) {
        if (this < imageWidth) {
            (imageWidth - this) * width / imageWidth
        } else {
            (this - imageWidth) * width / imageWidth
        }
    } else {
        this * width / imageWidth
    }
}


