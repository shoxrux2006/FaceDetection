package uz.gita.facedetection.data

import android.graphics.PointF
import android.graphics.Rect

data class FaceData(
    val faceLineData: MutableList<FaceLineData>,
    val rect: Rect,
    val color: Int
)
data class FaceLineData(
    val startPoint: PointF,
    val endPoint: PointF,
    val color:Int
)