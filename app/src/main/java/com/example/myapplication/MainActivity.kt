package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


class MainActivity : AppCompatActivity(), LocationListener {

    var mSettings: SharedPreferences? = null


    val APP_PREFERENCES = "mysettings"

    var APP_PREFERENCES_ADRES = "http://supl.ficom-it.info"
    var APP_PREFERENCES_TIME = "5"


    private lateinit var locationManager: LocationManager
    private lateinit var textCellTower: TextView
    private lateinit var longitudeText: TextView
    private lateinit var latitudeText : TextView
    private  lateinit var buttonCellTower:Button
    private  var isRun:Boolean = false
    lateinit var cellTowerHandlerl : Handler
    var leftTick:Int = 0
    lateinit var my_android_id:String
    lateinit var adresServer:EditText
    lateinit var intervalTime:EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textCellTower = findViewById(R.id.TextCellTower)
        buttonCellTower = findViewById(R.id.buttonCellTowe)
        cellTowerHandlerl = Handler(Looper.getMainLooper())
        latitudeText =  findViewById(R.id.LatitudeText)
        longitudeText = findViewById(R.id.LongitudeText)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        adresServer = findViewById(R.id.editTextTextPersonName)
        intervalTime = findViewById(R.id.editTextNumber)
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);



        Log.i("ANDROID ID", "##### READ Android ID ######")

        try {
             my_android_id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            Log.i("ANDROID ID", my_android_id)

        }
        catch (e: SecurityException){
            Log.i("ANDROID ID", "Secure Exception!")
        }
    }




    fun applySeting(view: View){
        val adresServerText = adresServer.getText().toString()
        val intervalTimeText = intervalTime.getText().toString()

        val editor: SharedPreferences.Editor = mSettings!!.edit()
        editor.putString(APP_PREFERENCES_ADRES, adresServerText)
        editor.putString(APP_PREFERENCES_TIME, intervalTimeText)
        editor.apply()

    }











    private val updateTextTask = object : Runnable{
        override fun run(){
            if(isRun){
                val timerSetting = mSettings?.getString(APP_PREFERENCES_TIME, "");
                APP_PREFERENCES_TIME = if((timerSetting != null)&&(timerSetting != "")){
                    timerSetting
                }else{
                    APP_PREFERENCES_TIME
                }
                cellTowerHandlerl.postDelayed(this, (APP_PREFERENCES_TIME.toInt()*1000).toLong())
            }
            getLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        cellTowerHandlerl.removeCallbacks(updateTextTask)
    }

    override fun onResume(){
        super.onResume()
        cellTowerHandlerl.post(updateTextTask)
    }





//    @RequiresApi(Build.VERSION_CODES.O)
    fun plusTick(myLocation: Location){

//        this.leftTick++
//        textCellTower.text = this.leftTick.toString()
        val telephony = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if ((ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) &&
//        (
//            ((ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_PRIVILEGED_PHONE_STATE
//            )) != PackageManager.PERMISSION_GRANTED
//        )) &&
        (
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            )) != PackageManager.PERMISSION_GRANTED
            )
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
                    Manifest.permission.INTERNET
                ),
                1
            )
            return
        }else{

            val cellLocation = telephony.allCellInfo
            val location = telephony.cellLocation as GsmCellLocation


            if (location != null) {
                textCellTower.setText("LAC: " + location.lac + " CID: " + location.cid)
                postData(telephony,location, myLocation)
            }
        }

    }


    fun getDataCellTown(view:View){
        buttonCellTower.text =
            if (this.isRun){
                this.onPause()
                "Старт"
            } else {
                this.onResume()
                "Стоп"
            };
        this.isRun = !this.isRun
    }

    private fun getLocation() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onLocationChanged(myLocation: Location) {
        val asdfs= latitudeText.text
        longitudeText.setText( "Долгота: " +  myLocation.longitude)
        latitudeText.setText("Широта:  "+ myLocation.latitude )
        plusTick(myLocation)
    }



    class CellTowerDataInfo(
        @SerializedName("ver") val ver: String?,
        @SerializedName("timestamp") val timestamp: String?,
        @SerializedName("uid") val uid: String?
        ) {}

//    class CellTowerDataRequest(
//        val request: String?
//    ) {}

    class cellTower1(
        @SerializedName("lac")val lac: Int,
        @SerializedName("cellid")val cellid: Int
    ) {}

    class CellTowerDataDataLog(
        @SerializedName("lat")val lat: String,
        @SerializedName("lon")val lon: String
    ) {}

    class CellTowerDataData(
        @SerializedName("celltower") val celtower: MutableList<cellTower1>,
        @SerializedName("loc") val loc: CellTowerDataDataLog,
        @SerializedName("device_id")   val device_id: String
    ) {}

     data class CellTowerData(
         @SerializedName("info") val info: CellTowerDataInfo,
         @SerializedName("request") val request: String?,
         @SerializedName("data") val data: CellTowerDataData
    ) {}
//    class CellTowerDeviceID(
//        val device_id: String,
//    ) {}


     data class CellTowerDataResult(
         @SerializedName("status") val status: String,
//         @SerializedName("message") val message: String,
//         @SerializedName("error") val error: String,
//         @SerializedName("data") val data: String,
     ) {
    }

    interface CellTowerClient {
        @Headers("Content-Type: application/json")
        @POST("/api.json/")
        fun SendTowerData(@Body data: CellTowerData): Call<CellTowerDataResult>

        @GET("/")
        fun getPage(): Call<String>
    }



    fun postData (telephony: TelephonyManager,GSMlocation:GsmCellLocation, myLocation: Location){

        val urlSetting = mSettings?.getString(APP_PREFERENCES_ADRES, "http://supl.ficom-it.info");

        APP_PREFERENCES_ADRES = if((urlSetting != null)&&(urlSetting != "")){
            urlSetting
        }else{
            APP_PREFERENCES_ADRES
        }


        val retrofit = Retrofit.Builder()
            .baseUrl(APP_PREFERENCES_ADRES)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CellTowerClient::class.java)


        val info: CellTowerDataInfo = CellTowerDataInfo("0.1", "1515", "15")
        val data: CellTowerDataData =
            CellTowerDataData(
                mutableListOf(cellTower1(GSMlocation.lac.toInt(), GSMlocation.cid.toInt())),
            CellTowerDataDataLog(myLocation.latitude.toString(), myLocation.longitude.toString()),
            my_android_id)
//        val device_id = CellTowerDeviceID(my_android_id)



        val theTowerData: CellTowerData = CellTowerData(info, "set_mob_geo_loc", data)
//        val theTowerData: CellTowerData =  CellTowerData( info, "set_mob_geo_loc" )

        var responce = service.SendTowerData(theTowerData).enqueue(
            object : Callback<CellTowerDataResult> {
                override fun onFailure(call: Call<CellTowerDataResult>, t: Throwable) {
                  Log.i("service","error")
                    Log.i("service",t.toString())
                }
                override fun onResponse( call: Call<CellTowerDataResult>, response: Response<CellTowerDataResult>) {
                    val page = response.body()
                    Log.i("service","OK")


                }
            })


    }

    }