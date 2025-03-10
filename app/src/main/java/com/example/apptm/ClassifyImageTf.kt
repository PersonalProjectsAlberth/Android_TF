package com.example.apptm

import android.content.Context
import android.graphics.Bitmap
import com.example.apptm.ml.ModelUnquant
import com.example.apptm.MainActivity.Companion.INPUT_SIZE
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ClassifyImageTf(val context:Context) {

    val modelTfp = ModelUnquant.newInstance(context)

    lateinit var returnInterpreter: ReturnInterpreter

    fun listenerInterpreter(returnInterpreter: ReturnInterpreter){
        this.returnInterpreter = returnInterpreter
    }

    fun classify(bitmap: Bitmap){

        val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 3), DataType.FLOAT32)
//        inputFeature0.loadBuffer(byteBuffer)
        val byteBuffer = ByteBuffer.allocateDirect(4* INPUT_SIZE* INPUT_SIZE*3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValue = IntArray(INPUT_SIZE* INPUT_SIZE)

        bitmap.getPixels(intValue, 0,bitmap.width, 0,0,bitmap.width,bitmap.height)

        for(pixelValue in intValue){
            byteBuffer.putFloat((pixelValue shr 16 and 0XFF)*(1f/255))
            byteBuffer.putFloat((pixelValue shr 8 and 0XFF)*(1f/255))
            byteBuffer.putFloat((pixelValue  and 0XFF)*(1f/255))
        }

        inputFeature.loadBuffer(byteBuffer)

        val output = modelTfp.process(inputFeature)
        val outputFeature = output.outputFeature0AsTensorBuffer
        val confidence = outputFeature.floatArray

        val maxPos = confidence.indices.maxByOrNull { confidence[it] }?:0

        returnInterpreter.classify(confidence,maxPos)
    }
}