package com.example.gps;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

@SuppressLint({ "NewApi", "NewApi", "NewApi" })
public class MainActivity extends Activity {

	TextView text;
	String lat;
	String lon;
	String alt;
	String acc;
	String provider;
	String phoneNumber;
	String date;
	String time;
	String street = "";
	String city = "";
	String country = "";
	String lastLon = "0";
	String lastLat = "0";
	
	LocationManager locationManager;
	ArrayList<GPSPoint> kolejka = new ArrayList<GPSPoint>();
	NetworkInfo currentNetworkStatus;
	ConnectivityManager conManager;
	LocationListener locationListener;
	
	final static int MIN_LOCATION_INTERVAL = 4*1000; 	// millis
	final static int MIN_LOCATION_DISTANCE = 2;			// meters
	
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        text = (TextView)findViewById(R.id.text);
        text.setText(
        		"MIN_LOCATION_INTERVAL: "+MIN_LOCATION_INTERVAL+" millis\n"+
        		"MIN_LOCATION_DISTANCE: "+MIN_LOCATION_DISTANCE+" meters"
        		);
        
        StrictMode.setThreadPolicy(policy);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        conManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//        currentNetworkStatus = conManager.getActiveNetworkInfo();
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
        phoneNumber =  mTelephonyMgr.getLine1Number();
//        phoneNumber =  "666666666";
        
        locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {	
				lat = Double.toString(location.getLatitude());
				lon = Double.toString(location.getLongitude());
				alt = Double.toString(location.getAltitude());
				acc = Double.toString(location.getAccuracy());
				provider = location.getProvider();
				
				System.out.println(lat +" : "+ lon);
//				double d = getDistanse(lastLat, lat, lastLon, lon);
//				System.out.println(d+"m");
				
				
				SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
				date = sdfDate.format(new Date());
				time = sdfTime.format(new Date());
				
				Geocoder geocoder;
				List<Address> addresses;
				geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				
				if (currentNetworkStatus != null && currentNetworkStatus.isConnected()) {
					try {
						addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
						street = addresses.get(0).getAddressLine(0);
						city = addresses.get(0).getAddressLine(1);
						country = addresses.get(0).getAddressLine(2);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} 
				
				if (kolejka.size() != 0 
						&& !kolejka.get(kolejka.size()-1).getLat().equals(lat) 
						&& !kolejka.get(kolejka.size()-1).getLon().equals(lon) ) {
							kolejka.add(new GPSPoint(lat, lon, alt, acc, provider, phoneNumber, date, time, city, street, country));
							updateText();
							currentNetworkStatus = conManager.getActiveNetworkInfo();
							if (currentNetworkStatus != null && currentNetworkStatus.isConnected()) {
								sendData();
								lastLon = lon;
								lastLat = lat;
								kolejka.clear();
							}
				} else if(kolejka.size() == 0){
//					if (d >= 10) {
						kolejka.add(new GPSPoint(lat, lon, alt, acc, provider, phoneNumber, date, time, city, street, country));
						updateText();
						currentNetworkStatus = conManager.getActiveNetworkInfo();
						if (currentNetworkStatus != null && currentNetworkStatus.isConnected()) {
							sendData();
							lastLon = lon;
							lastLat = lat;
							kolejka.clear();
						}
//					}
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
			}
        	
        };
        
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_LOCATION_INTERVAL, MIN_LOCATION_DISTANCE, locationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_exit:
            	finish();
            	System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void sendData(){
    	
    	try {
    		HttpClient client = new DefaultHttpClient();
    		HttpGet request;
    		HttpResponse response;
    		for (int i = 0; i < kolejka.size(); i++) {
    			String reqString = "http://www.web-artz.pl/gps.php?lat="+kolejka.get(i).getLat()+"&lon="+kolejka.get(i).getLon()+"&alt="+kolejka.get(i).getAlt()+"&acc="+kolejka.get(i).getAcc()+"&provder="+kolejka.get(i).getProvider()+"&id="+kolejka.get(i).getId()+"&date="+kolejka.get(i).getDate()+"&time="+kolejka.get(i).getTime();
    			request = new HttpGet(reqString);
    			response = client.execute(request);
    			String debug = (String) text.getText();
    			debug += "\n"+response.getStatusLine();
    			text.setText(debug);
    			System.out.println(response.getStatusLine());
			}
			
		} catch (ClientProtocolException e) {
			kolejka.add(new GPSPoint(lat, lon, alt, acc, provider, phoneNumber, date, time, city, street, country));
		} catch (IOException e) {
			kolejka.add(new GPSPoint(lat, lon, alt, acc, provider, phoneNumber, date, time, city, street, country));
		}
    }
    
    public void updateText(){
    	String kolejkaString = "";
		for (int i = 0; i < kolejka.size(); i++) {
			kolejkaString += "Lat: "+kolejka.get(i).getLat()+"\n";
			kolejkaString += "Lon: "+kolejka.get(i).getLon()+"\n";
			kolejkaString += "Alt: "+kolejka.get(i).getAlt()+"\n";
			kolejkaString += "Acc: "+kolejka.get(i).getAcc()+"\n";
			kolejkaString += "Provider: "+kolejka.get(i).getProvider()+"\n";
			
			if (kolejka.get(i).getStreet()!="" && kolejka.get(i).getCity()!="" && kolejka.get(i).getCountry()!="") {
				kolejkaString += kolejka.get(i).getStreet()+", "+kolejka.get(i).getCity()+", "+kolejka.get(i).getCountry()+"\n";
			}
			
			kolejkaString += kolejka.get(i).getDate()+" "+kolejka.get(i).getTime()+"\n\n";
		}
		text.setText(kolejkaString+"QUEUE SIZE::"+kolejka.size());
    }
    
    public double getDistanse(String lati1, String lati2, String lont1, String lont2){
    	int R = 6371; // km
		double dLat = Math.toRadians((Double.parseDouble(lati1) - Double.parseDouble(lati2)));
		
		double dLon = Math.toRadians((Double.parseDouble(lont1) - Double.parseDouble(lont2)));
		double lat1 = Math.toRadians(Double.parseDouble(lati1));
		double lat2 = Math.toRadians(Double.parseDouble(lati2));

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		return d*1000;
    }
}
