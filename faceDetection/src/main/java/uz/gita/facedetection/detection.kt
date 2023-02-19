package uz.gita.facedetection

import android.graphics.Color
import com.google.mlkit.vision.face.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import uz.gita.facedetection.data.CaptureData
import uz.gita.facedetection.data.FaceData
import uz.gita.facedetection.data.FaceLineData
import uz.gita.facedetection.drawer.DrawFaceLine


fun faceContourDetectionResult(
    face: Face,
    isPortrait: Boolean,
    view: DrawFaceLine
): Flow<CaptureData> = flow {
    val headEulerAngleY = face.headEulerAngleY
    val headEulerAngleX = face.headEulerAngleX
    val leftEyeOpenProbability = face.leftEyeOpenProbability
    val rightEyeOpenProbability = face.rightEyeOpenProbability
    val left: Float
    val right: Float
    val top: Float
    val bottom: Float
    val faceRight: Float
    val faceLeft: Float
    if (isPortrait) {
        faceLeft = face.boundingBox.right.toFloat()
        faceRight = face.boundingBox.left.toFloat()
        left = view.width * 0.11f
        right = view.width * 0.88f
        bottom = view.height * 0.8f
        top = view.height * 0.2f
    } else {
        faceLeft = face.boundingBox.right.toFloat()
        faceRight = face.boundingBox.left.toFloat()
        left = view.width * 0.28f
        right = view.width * 0.71f
        bottom = view.height.toFloat()
        top = view.height * 0.01f
    }
    when {
        !(
                faceRight >= left
                        &&
                        faceLeft <= right
                )
        -> {
            emit(CaptureData.Error("Yuzingiz doiradan tashqarida"))
        }
        !(face.boundingBox.bottom <= bottom &&
                face.boundingBox.top >= top
                ) -> {
            emit(CaptureData.Error("Yuzingiz doiradan tashqarida"))
        }
        !(headEulerAngleX >= -15 &&
                headEulerAngleX <= 15)
        -> {
            emit(CaptureData.Error("yuzingizni teparoq yoki pastroq tushiring"))
        }
        !(headEulerAngleY >= -10 &&
                headEulerAngleY <= 10) -> {
            emit(CaptureData.Error("yuzingizni o'ng yoki chapga buring"))
        }
        !(rightEyeOpenProbability != null
                && rightEyeOpenProbability > 0.7
                && leftEyeOpenProbability != null
                && leftEyeOpenProbability > 0.7) -> {
            emit(CaptureData.Error("ko'zingizni oching"))
        }
        else -> {
            emit(CaptureData.Success)
        }
    }

    var faceData: FaceData? = null
    var list: MutableList<FaceLineData> = mutableListOf()

    val faceOval = face.getContour(FaceContour.FACE)?.points
    faceOval?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.GREEN))
        }
        list.add(FaceLineData(startPoint = it[0], endPoint = it[it.size - 1], Color.GREEN))

    }
    val noseBridge = face.getContour(FaceContour.NOSE_BRIDGE)?.points
    noseBridge?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.BLUE))
        }
    }
    val noseBottom = face.getContour(FaceContour.NOSE_BOTTOM)?.points
    noseBottom?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.GRAY))
        }

    }


    val rightEyeBrowTop = face.getContour(FaceContour.RIGHT_EYEBROW_TOP)?.points
    rightEyeBrowTop?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.CYAN))
        }
    }
    val leftEyeBrowTop = face.getContour(FaceContour.LEFT_EYEBROW_TOP)?.points
    leftEyeBrowTop?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }


    val rightEyeBrowBottom = face.getContour(FaceContour.RIGHT_EYEBROW_BOTTOM)?.points
    rightEyeBrowBottom?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }
    val leftEyeBrowBottom = face.getContour(FaceContour.LEFT_EYEBROW_BOTTOM)?.points
    leftEyeBrowBottom?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }
    val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
    val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
    val rightEyeContour = face.getContour(FaceContour.RIGHT_EYE)?.points

    leftEyeContour?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }
    rightEyeContour?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }

    upperLipBottomContour?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }

    val upperLipTopContour = face.getContour(FaceContour.UPPER_LIP_TOP)?.points
    upperLipTopContour?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }

    val lowerLipBottomContour = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points
    lowerLipBottomContour?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
    }

    val lowerLipTopContour = face.getContour(FaceContour.LOWER_LIP_TOP)?.points
    lowerLipTopContour?.toList()?.let {
        for (i in 1 until it.size) {
            list.add(FaceLineData(startPoint = it[i], endPoint = it[i - 1], Color.MAGENTA))
        }
        faceData = FaceData(list, face.boundingBox, Color.RED)
    }
    faceData?.let {
        emit(CaptureData.LoadFaceData(it))
    }
}.flowOn(Dispatchers.Default)



