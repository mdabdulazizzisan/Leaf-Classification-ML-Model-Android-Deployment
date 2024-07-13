package com.kolu.medicinalleafclassification

import android.content.Context
import android.graphics.Bitmap
import com.kolu.medicinalleafclassification.ml.Densenet201Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RunModel {
    fun run(context: Context, byteBuffer: ByteBuffer): FloatArray? {
        val model = Densenet201Model.newInstance(context)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidence = outputFeature0.floatArray
        // Releases model resources if no longer used.
        model.close()

        return confidence
    }

    fun preprocess(image: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4).apply { order(ByteOrder.nativeOrder()) }

        val tensorImage = TensorImage.fromBitmap(image)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val resizedImage = imageProcessor.process(tensorImage)

        val pixels = IntArray(224 * 224)
        resizedImage.bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF).toFloat() / 255.0f
            val g = (pixel shr 8 and 0xFF).toFloat() / 255.0f
            val b = (pixel and 0xFF).toFloat() / 255.0f
            imgData.putFloat(r)
            imgData.putFloat(g)
            imgData.putFloat(b)
        }

        return imgData
    }

}