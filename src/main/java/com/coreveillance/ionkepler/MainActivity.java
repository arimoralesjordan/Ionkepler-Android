/*
 * Copyright (C) 2016 Ari Morales arimoralesjordan@gmail.com
 *
 *
 */
package com.coreveillance.ionkepler;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity{

    public static final String PREFS_NAME = "ionkepler_credential";
    private static final String TAG = MainActivity.class.getSimpleName();
    private XWalkView myWebView;
    private Double latitud = 0.00;
    private Double longitud = 0.00;
    private Double altitud = 0.00;
    private Float speed;
    private Float accuracy;
    private String input_serial = "";
    private String SignatureResponde = "";
    private String function = "";
    private String user = "";
    private String password = "";
    private String url = "http://192.168.1.233/coreveillance/main/mobile.php";
    private JSONObject obj;
    private LocationManager locationManager;
    private String locationProvider;
    
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] CAMERA_PERMS={
            Manifest.permission.CAMERA
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int CAMERA_REQUEST=INITIAL_REQUEST+1;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+2;
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                UpdateGpsData(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
                locationProvider = LocationManager.GPS_PROVIDER;
            }
            public void onProviderEnabled(String provider) {
                Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
                locationProvider = LocationManager.GPS_PROVIDER;
            }
            public void onProviderDisabled(String provider) {
                Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
                locationProvider = LocationManager.NETWORK_PROVIDER;
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            return;
        }
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        SharedPreferences sharedpreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Global Global=new Global();
        XWalkPreferences.setValue("enable-javascript", true);
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        myWebView = (XWalkView) findViewById(R.id.webview);
        myWebView.addJavascriptInterface(new WebAppInterface(this, myWebView), "Android");
        myWebView.clearCache(true);
        myWebView.load(url,null);
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!canAccessLocation() || !canAccessCamera()) {
            Toast.makeText(getBaseContext(), "Ionkepler needs to use your Location and Camera", Toast.LENGTH_LONG).show();
        }
        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }
    private boolean canAccessCamera() {
        return(hasPermission(Manifest.permission.CAMERA));
    }
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }
    public void UpdateGpsData(Location location) {
        latitud=location.getLatitude();
        longitud=location.getLongitude();
        altitud=location.getAltitude();
        speed=location.getSpeed();
        accuracy=location.getAccuracy();
    }

    public Context getActivity() {
        return this;
    }

    public class WebAppInterface {
        Context mContext;
        private XWalkView xWalkWebView;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c,XWalkView xWalkWebView) {
            mContext = c;
            this.xWalkWebView = xWalkWebView;
        }
        @org.xwalk.core.JavascriptInterface
        public void postMessage(String sentData) throws JSONException {
            obj = new JSONObject(sentData);
            function = obj.getString("fnc");
            if (function.equals("activate_scanner")){
                input_serial=obj.getString("input_serial");
                MainActivity.this.activate_scanner();
            }
            if (function.equals("ShowRouteMap")){
                String daddress=obj.getString("daddress");
                MainActivity.this.ShowRouteMap(daddress);
            }
            if (function.equals("ESignature")){
                SignatureResponde=obj.getString("respond");
                String customer_name=obj.getString("customer_name");
                MainActivity.this.ESignature(customer_name);
            }
            if (function.equals("SaveSession")){
                myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MainActivity.this.SaveSession(obj.getString("user"),obj.getString("password"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (function.equals("GetSession")){
                myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.GetSession();
                    }
                });
            }
            if (function.equals("DeleteSession")){
                myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.SaveSession("","");
                    }
                });
            }
            if (function.equals("GetGPSLocation")){
                myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        String input= null;
                        try {
                            input = obj.getString("input");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MainActivity.this.Location(input);
                    }
                });
            }
        }
    }
    public void Location(String input){
        Log.d(TAG, "GPS Location");
        String location="{\"lat\":\""+latitud+"\",\"long\":\""+longitud+"\",\"altitud\":\""+altitud+"\",\"speed\":\""+speed+"\",\"accuracy\":\""+accuracy+"\"}";
        String script="$('#"+input+"').val('"+location+"');CallbackNativeApp();";
        Log.d(TAG, "Sending: "+script);
        myWebView.load("javascript:"+script,null);
    }
    public void SaveSession(String user, String password){
        Log.d(TAG, "Saving Session");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", user);
        editor.putString("password", password);
        editor.apply();
    }
    public void GetSession(){
        Log.d(TAG, "Getting Session");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        user = sharedPreferences.getString("user","");
        password = sharedPreferences.getString("password","");
        String script="javascript:xajax_controlador('iniciar_sesion',{user:'"+user+"',password:'"+password+"'});";
        Log.d(TAG, script);
        myWebView.load("javascript:xajax_controlador('iniciar_sesion',{user:'"+user+"',password:'"+password+"'});",null);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    public void ShowRouteMap(String daddress){
        Log.d(TAG, "Showing Route");
        Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + daddress);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);

    }
    public void ESignature(String customer_name) {
        Intent intent = new Intent(this, signature.class);
        startActivityForResult(intent, 1);
    }
    public void activate_scanner() {
        Log.d(TAG, "Loading ESignature");
        Intent intent = new Intent(this, ContinuousCaptureActivity.class);
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (function.equals("activate_scanner")){
            for (BarCodes.serial serial : Global.barCodes.serials) {
                String script="javascript:load_serial({serial:'"+serial.number+"',product_number:'"+serial.product+"',input_serial:'"+input_serial+"'})";
                Log.d(TAG, script);
                myWebView.load("javascript:load_serial({serial:'"+serial.number+"',product_number:'"+serial.product+"',input_serial:'"+input_serial+"'})",null);
            }
            Global.barCodes.serials.clear();
        }else if (function.equals("ESignature")){
            String signature=Global.Esignature;
            signature=signature.replaceAll("(\\r|\\n)", "");
            if (!signature.equals("")){
                String script="javascript:put_src_img('"+SignatureResponde+"_image','data:image/png;base64,"+signature+"')";
                Log.d(TAG, script);
                myWebView.load("javascript:put_src_img('"+SignatureResponde+"_image','data:image/png;base64,"+signature+"')",null);
            }
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if(resultCode == Activity.RESULT_OK) {
                if(data == null) {
                    // If there is not data, then we may have taken a photo
                    if(mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
            return;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
