package com.example.app_map;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity /*implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener */{
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn1=findViewById(R.id.btn1);
        Button btn2=findViewById(R.id.btn2);
        Button btn_env=findViewById(R.id.btn_env);
        Button btn_respo=findViewById(R.id.btn_respo);
        Button btn_pas=findViewById(R.id.btn_pas);

        //test serv
        if(!runtime_permissions()&& !isMyServiceRunning(Traitement.class)){
            Toast.makeText(this, "permission ok", Toast.LENGTH_SHORT).show();
            Intent i =new Intent(getApplicationContext(),Traitement.class);
            startService(i);

        }

        btn_env.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });


        mRequestQue = Volley.newRequestQueue(this);

        btn_respo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().subscribeToTopic("lala").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "sub reussi";
                        if (!task.isSuccessful()) {
                            msg = "sub echoué";
                        }
                        Log.d("SUB", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btn_pas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("lala").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "unsub reussi";
                        if (!task.isSuccessful()) {
                            msg = "unsub echoué";
                        }
                        Log.d("unSUB", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


//////db.////////////////


      /*  //polygon add
        polygon.add(new LatLng(31.644482,-8.011921));
        polygon.add(new LatLng(31.644486,-8.019021));
        polygon.add(new LatLng(31.644367,-8.019006));
        polygon.add(new LatLng(31.644320,-8.019127));
        polygon.add(new LatLng(31.644387,-8.019175));*/


        //request 25/12/2019
     /*   mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();*/



        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CircleDemoActivity.class);
                startActivity(intent);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ConsulterActivity.class);
                startActivity(intent);
            }
        });

        //level of battery


        intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        MainActivity.this.registerReceiver(broadcastreceiver,intentfilter);

    }

    @Override
    protected void onResume() {
        super.onResume();


    }
    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    IntentFilter intentfilter;
    int deviceStatus;
    String currentBatteryStatus="Battery Info";
    int batteryLevel;
    //receive the new value of baterry if the level of battery  changes
    private BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryLevel=(int)(((float)level / (float)scale) * 100.0f);
            Toast.makeText(MainActivity.this,batteryLevel+"%" , Toast.LENGTH_SHORT).show();

            // test if the level of battery id lower
            if(batteryLevel<=15){
                Toast.makeText(MainActivity.this,"Attention Battery is  "+batteryLevel+"%" , Toast.LENGTH_SHORT).show();
                SmsManager.getDefault().sendTextMessage("0641385919", null, "Attention le niveau de batterie de "+Traitement.user+ "est moins de 15%" , null, null);

            }

        }
    };

    private void sendNotification() {

        JSONObject json = new JSONObject();

        try {
            json.put("to","/topics/"+"lala");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title","Notification");
            notificationObj.put("body","La personne est hors des zones spécifiées");





            json.put("notification",notificationObj);



            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AIzaSyAy06twL__KcFW6qfPudnjizqle681t-iw");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
