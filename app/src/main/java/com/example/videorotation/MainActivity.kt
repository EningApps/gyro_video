package com.example.videorotation

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), SensorEventListener {

    private val DELTA_ANGLE = 57.2957795f

    private val ALPHA = 0.08f;

    private lateinit var sensorManager: SensorManager

    private var gravity: FloatArray? = null
    private var magnetic: FloatArray? = null
    private var accels: FloatArray? = FloatArray(3)
    private var mags: FloatArray? = FloatArray(3)
    private var values = FloatArray(3)

    private var azimuth: Float = 0.toFloat()
    private var pitch: Float = 0.toFloat()
    private var roll: Float = 0.toFloat()//y

    private val yValues = ShiftQueue(10)
    private val filteredValues = ShiftQueue(10)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        videoTexture.resId = R.raw.video_nature
        videoTexture.onVideoCompleteListener = { videoTexture.play() }
        videoTexture.scaleX = 2f
        videoTexture.scaleY = 2f
    }

    override fun onResume() {
        super.onResume()

        videoTexture.play()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.getType()) {
            Sensor.TYPE_MAGNETIC_FIELD ->
                mags = event.values.clone()
            Sensor.TYPE_ACCELEROMETER ->
                accels = event.values.clone()
        }

        if (mags != null && accels != null) {
            gravity = FloatArray(9)
            magnetic = FloatArray(9)
            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
            val outGravity = FloatArray(9)
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X, SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);

            azimuth = values[0] * DELTA_ANGLE
            pitch = values[1] * DELTA_ANGLE
            roll = values[2] * DELTA_ANGLE
            mags = null
            accels = null

            yValues.put(roll)

            lowPass(yValues.values, filteredValues.values)

            updateVideoViewRotation()
        }
    }

    private fun updateVideoViewRotation() {
        videoTexture.rotation = -filteredValues.get()
    }

    private fun lowPass(input: FloatArray, output: FloatArray): FloatArray {
        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }

}