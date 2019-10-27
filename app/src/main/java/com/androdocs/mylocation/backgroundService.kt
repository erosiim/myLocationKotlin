package com.androdocs.mylocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.android.gms.location.*
import java.util.*
import kotlin.math.abs
import android.R
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.*
import android.util.Log
import android.os.Bundle
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Criteria
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class backgroundService: Service(){
    //Tomamos el acelerómetro
    private var sensorManager: SensorManager?=null
    private var lastUpdate: Long = 0
    private var mlocation: Location ?= null;




    var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            mlocation = location
            Log.d("Location Changes", location.toString())

        }

         fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d("Status Changed", status.toString())
        }

        fun onProviderEnabled(provider: String) {
            Log.d("Provider Enabled", provider)
        }

        fun onProviderDisabled(provider: String) {
            Log.d("Provider Disabled", provider)
        }
    }

    //Validamos permisos
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    //Hacemos el servicio
    private lateinit var mHandler: Handler

    private lateinit var mRunnable: Runnable
    private var listen : SensorListen?=null
    var xold=0.0



    var yold=0.0
    var zold=0.0
    var threadShould=1850.0
    var oldtime:Long=0
    override fun onBind(intent: Intent): IBinder? {
        return null;
    }


    inner class MylocationListener: LocationListener {
        override fun onLocationChanged(location: Location) {
            mlocation = location
            Log.d("Location Changes", location.toString())

        }

        fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d("Status Changed", status.toString())
        }

        fun onProviderEnabled(provider: String) {
            Log.d("Provider Enabled", provider)
        }

        fun onProviderDisabled(provider: String) {
            Log.d("Provider Disabled", provider)
        }
    }

    override fun onCreate() {
        // TODO Auto-generated method stub
        Toast.makeText(applicationContext, "Started", Toast.LENGTH_SHORT ).show()
        super.onCreate()
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        lastUpdate = System.currentTimeMillis()
        sensorManager=getSystemService(Context.SENSOR_SERVICE)as SensorManager
        var sensor: Sensor=sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        listen= SensorListen()
        sensorManager!!.registerListener(listen , sensor,SensorManager.SENSOR_DELAY_UI)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        sensorManager?.unregisterListener(listen)
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

    // Custom method to do a task
    private fun showRandomNumber() {
        val rand = Random()
        val number = rand.nextInt(100)
        //toast("Random Number : $number")
        Toast.makeText(applicationContext,"Random Number : $number", Toast.LENGTH_SHORT).show()
        mHandler.postDelayed(mRunnable, 3500)
    }
    inner class SensorListen : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            var x=event!!.values[0]
            var y=event!!.values[1]
            var z=event!!.values[2]
            var currentTime=System.currentTimeMillis()
            if((currentTime-oldtime)>100){
                var timeDiff=currentTime-oldtime
                oldtime=currentTime
                var speed= abs(x+y+z-xold-yold-zold) /timeDiff*10000
                if(speed>threadShould){
                    var criteria : Criteria= Criteria()
                    criteria.setAccuracy(Criteria.ACCURACY_COARSE)
                    criteria.setPowerRequirement(Criteria.POWER_LOW)
                    criteria.setAltitudeRequired(false)
                    criteria.setBearingRequired(false)
                    criteria.setSpeedRequired(false)
                    criteria.setCostAllowed(true)
                    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH)
                    criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH)
                    var v=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    v.vibrate(500)
                    Toast.makeText(applicationContext,"shock me caí gg",Toast.LENGTH_LONG).show()


                    var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0.1f,mylocation)
                    //locationManager.requestSingleUpdate(criteria, locationListener, Looper.myLooper())
                    locationManager.requestSingleUpdate( criteria ,locationListener!!,  Looper.getMainLooper())

                }
            }
        }




        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // TODO Auto-generated method stub
        }


        private fun requestNewLocationData() {
            var mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            mLocationRequest.numUpdates = 1

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }

        private val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var mLastLocation: Location = locationResult.lastLocation
                var txt = mLastLocation.latitude.toString()
                var txt2 = mLastLocation.longitude.toString()
            }
        }

        private fun isLocationEnabled(): Boolean {
            var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }


    }

}



