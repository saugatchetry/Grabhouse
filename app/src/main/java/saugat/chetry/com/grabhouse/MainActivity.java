package saugat.chetry.com.grabhouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {


    File filePath,photoDirectory;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private ArrayList<String> addressKeys = new ArrayList<String>();
    boolean internetPresent = false;
    boolean gpsPresent = false;
    Context context = this;
    ConnectivityManager connectivityManager;
    LocationManager locationManager;
    MyPhotoAdapter adapter;
    ListView lv_allPhotos;
    int counter = 0;
    ProgressDialog dialog;
    String myCurrentAddress = "Address Not Found but You are on Earth";
    SharedPreferences prefs;
    String name;
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("MyAddresses",Context.MODE_PRIVATE);
        //make the directory
        maketheDirectory();

        //photoDirectory = new File(Environment.getExternalStorageDirectory() + "/AppPhotos", System.currentTimeMillis() + ".jpg");
        //read the files in the directory
        //getImagePaths();

        //get reference to ListView
        lv_allPhotos = (ListView) findViewById(R.id.photoList);


        getImagePaths();

        if(counter == 0)
        {
            Toast.makeText(this,"Click your 1st image",Toast.LENGTH_LONG).show();
        }

        adapter = new MyPhotoAdapter(MainActivity.this,imagePaths,80,addressKeys);

        lv_allPhotos.setAdapter(adapter);

        //initialize custom adapter for the listview
        //adapter = new MyPhotoAdapter(MainActivity.this,imagePaths,80);

        //lv_allPhotos.setAdapter(adapter);

        //readSavedAddresses();


    }

    private void readSavedAddresses() {

        String savedAddress = prefs.getString(name,myCurrentAddress);
        Log.d("Reading Address = ",savedAddress);
    }

    private void getImagePaths() {

        File[] files = new File(String.valueOf(filePath)).listFiles();
        for (File file : files)
        {
            if (file.isFile())
            {
                String path = filePath.toString()+"/"+file.getName();
                imagePaths.add(path);
                Log.d("Path",filePath+"/"+file.getName());
                String key = path.substring(30,43);

                String savedAddress = prefs.getString(key,myCurrentAddress);
                addressKeys.add(savedAddress);
                Log.d("Key",savedAddress);
            }
        }
        counter = imagePaths.size();
    }

    private void maketheDirectory() {

        filePath = new File(Environment.getExternalStorageDirectory(), "/AppPhotos/");
        if(!filePath.exists())
        {
            filePath.mkdirs();
        }
        Log.d("FilePath",filePath.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*@Override
    protected void onPause() {

        mLocationManager.removeUpdates(mLocationListener);

    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so longP
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_photo)
        {
            internetPresent = isInternetOn();
            gpsPresent = isGPSOn();

            if(internetPresent == true && gpsPresent == true)
            {
                name = String.valueOf(System.currentTimeMillis());
                //photoDirectory = new File(Environment.getExternalStorageDirectory() + "/AppPhotos", System.currentTimeMillis() + ".jpg");
                photoDirectory = new File(Environment.getExternalStorageDirectory()+"/AppPhotos",name+".jpg");
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                Uri tempUri = Uri.fromFile(photoDirectory);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(cameraIntent, 1337);

                /*AddressFinder addressFinder = new AddressFinder(MainActivity.this);
                addressFinder.execute();*/

                dialog = ProgressDialog.show(MainActivity.this,"Getting Location","Please Wait ....");
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(name,myCurrentAddress);
                        editor.commit();
                        Log.d("Name :- ",name);
                        Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
                    }
                });

                configGPS();


            }

            else if (!internetPresent)
            {
                showInternetAlert();
            }

            else if(!gpsPresent)
            {
                showGPSAlert();
            }

            //Toast.makeText(MainActivity.this,"Internet "+isInternetOn()+" GPS "+isGPSOn(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListener);
        }
        finish();
    }

    @Override
    protected void onResume() {
        Log.d("Activity","Onresume called");

       /* lv_allPhotos.setAdapter(null);
        imagePaths.clear();
        addressKeys.clear();

        getImagePaths();

        adapter = new MyPhotoAdapter(MainActivity.this,imagePaths,80,addressKeys);

        lv_allPhotos.setAdapter(adapter);
        Log.d("Length",""+addressKeys.size());*/
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1337) {

            switch (resultCode) {
                case Activity.RESULT_OK:

                    if (photoDirectory.exists()) {
                        Toast.makeText(this, "Saved at " + photoDirectory.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        //Log.d("Location ",locationAddress);
                        //saveInfo();


                        lv_allPhotos.setAdapter(null);
                        imagePaths.clear();
                        addressKeys.clear();

                        getImagePaths();

                        adapter = new MyPhotoAdapter(MainActivity.this,imagePaths,80,addressKeys);

                        lv_allPhotos.setAdapter(adapter);
                        Log.d("Length",""+addressKeys.size());


                    } else {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case Activity.RESULT_CANCELED:
                    break;

                default:
                    break;
            }


            /*//  data.getExtras()
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ImageView image =(ImageView) findViewById(R.id.PhotoCaptured);
            image.setImageBitmap(thumbnail);*/
        } else {
            Toast.makeText(this, "Picture NOt taken", Toast.LENGTH_LONG);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void showGPSAlert()
    {


        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("GPS Disabled");
        dialog.setMessage("GPS Disabled. Do you want to go to settings menu ?");
        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    public void showInternetAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("No Internet");
        alertDialog.setMessage("Internet is Disabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                MainActivity.this.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isGPSOn();
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    public boolean isInternetOn()
    {
        connectivityManager = (ConnectivityManager) getSystemService(getApplication().CONNECTIVITY_SERVICE);

        if (connectivityManager.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED)
        {

            return true;

        }
        else if(connectivityManager.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                connectivityManager.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED)
        {


            return false;
        }

        return false;
    }


    public boolean isGPSOn()
    {

        boolean gps_enabled,network_enabled;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!gps_enabled && !network_enabled)
        {
            return false;

        }

        return true;

    }

    private void configGPS() {

       /* LocationManager mLocationManager;
        LocationListener mLocationListener;*/
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new MyLocationListener();

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,mLocationListener);

    }

    private void updateUI(Location loc)
    {
        //myLocation.setText("Latitude :- "+loc.getLatitude()+"\nLongitude :- "+loc.getLongitude());
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try
        {
            List<Address> address = geocoder.getFromLocation(loc.getLatitude(),loc.getLongitude(),1);
            if(address.size() > 0)
            {
                String addr = "";
                for(int i = 0; i < address.get(0).getMaxAddressLineIndex(); i++)
                {

                    addr += address.get(0).getAddressLine(i)+"\n";
                }
                //Toast.makeText(getBaseContext(),addr,Toast.LENGTH_SHORT).show();
                //myLocation.setText(addr);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(name,addr);
                editor.commit();
                Log.d("Name :- ",name);
            }
        }

        catch(Exception e)
        {

        }
        dialog.dismiss();

    }


    class MyLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location location) {

            Log.d("Location", "Latitude :- "+location.getLatitude()+" Longitude :- "+location.getLongitude());
            updateUI(location);


        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }


}
