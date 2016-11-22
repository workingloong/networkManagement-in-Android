package com.example.networkmanager;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {
	private WifiManager wifiManager = null;
	private static final String TAG = MainActivity.class.getSimpleName();
	private double latitude = 0.0;
	private double longitude = 0.0;
	private TextView info;
	private LocationManager locationManager = null;
	private TextView fileText;
	private Handler handler = new Handler();
	private Context mContext;
	private int dayIndex;
	private String dateStr;
	private TextView recordNumText;
	private TextView netInfoText;
	private String lastContent = "";
	private double lastLatitude = 0.0;
	private double lastLongitude = 0.0;
	private int lastMinute = 0;
	private LocationListener locationListener = new LocationListener() {
		// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		// Provider被enable时触发此函数，比如GPS被打开
		@Override
		public void onProviderEnabled(String provider) {
			Log.e(TAG, provider);
		}

		// Provider被disable时触发此函数，比如GPS被关闭
		@Override
		public void onProviderDisabled(String provider) {
			Log.e(TAG, provider);
		}
		
		// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
				latitude = location.getLatitude(); // 经度
				longitude = location.getLongitude(); // 纬度
			}
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wifiManager   = (WifiManager) super.getSystemService(Context.WIFI_SERVICE);
		info          = (TextView) findViewById(R.id.textView1);
		fileText      = (TextView) findViewById(R.id.textView2);
		recordNumText = (TextView) findViewById(R.id.textView3);
		netInfoText   = (TextView) findViewById(R.id.textView4);
		mContext      = getApplicationContext();

		//获取按钮控件，设置事件监听
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new ButtonListener01());
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Runnable GPSrunnable = new Runnable(){
			public void run(){
				getLocationByGPS();
				getLocationByBaseStation();
				getCurrentTime();
				dataToFile();
				info.setText("纬度：" + lastLatitude + "\n" + "经度：" + lastLongitude);
				handler.postDelayed(this,30000);//每5s获取一次位置信息和时间信息
			}
		};
		//判断GPS是否打开，
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//getLocationByGPS();
			handler.postDelayed(GPSrunnable,1000); 
		}else{
			Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
			handler.postDelayed(GPSrunnable,1000); 
		}
		
	}
	public void dataToFile(){
		String filename  = "network_info_log.txt";
		FileHelper fhelper = new FileHelper(mContext,filename);
		int networkStatus = 0;
		networkStatus = getNetworkStatus();
		if(lastLatitude != latitude || lastLongitude != longitude){
			lastLatitude = latitude;
			lastLongitude = longitude;
		}
		else{
			latitude = 0.0;
			longitude = 0.0;
		}
		String fileContent = dateStr+","        //日期时间
				+String.valueOf(dayIndex-1)+"," // 星期
				+String.valueOf(latitude)+","   //经度
				+String.valueOf(longitude)+","  //纬度
				+netInfoText.getText().toString()+ "," //接入的基站信息
				+String.valueOf(networkStatus)+"\n" ;  //上网所使用的网络WiFi或者LTE
		String currentContent = fileContent.substring(20);

		int currentMinute = Integer.parseInt(fileContent.substring(14,16));
		if(currentContent.equals(lastContent) && Math.abs(currentMinute - lastMinute) < 5) return;

		String detail = fhelper.readData();
		fileText.setText(detail);
		String[] logs = detail.split("\n");
		recordNumText.setText("记录数： "+String.valueOf(logs.length));
		if(logs.length > 9){
			fhelper.dataSave(fileContent,Context.MODE_PRIVATE);
		}
		else{
			fhelper.dataSave(fileContent,Context.MODE_APPEND);
		}
		lastContent = currentContent;
		lastMinute = currentMinute;
	}
	/*
	public void toggleGPS(){
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings", "aom.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try{
			PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
		}catch (CanceledException e) {
			e.printStackTrace();
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
			Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location1 != null) {
				latitude = location1.getLatitude(); // 经度
				longitude = location1.getLongitude(); // 纬度
			}
		}
	}*/
	private int getNetworkStatus(){
		if(wifiManager.isWifiEnabled()){
			return 1;
		}
		else if(getMobileDataState()){
			return 2;
		}
		else return 0;
	}
	
	private void getLocationByGPS() {
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		} 
		else {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
		}
	}
	
	private void getCurrentTime(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		dateStr = format.format(date);
		dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	// 获取基站的位置信息
    private void getLocationByBaseStation() {  
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);  
        GsmCellLocation gsmCell = (GsmCellLocation) tm.getCellLocation();  
        int cid = gsmCell.getCid();  //获取基站编号
        int lac = gsmCell.getLac();  //获取位置区域码
        String netOperator = tm.getNetworkOperator();  
        int mcc = Integer.valueOf(netOperator.substring(0, 3));  //移动网络国家代码，中国为460
        int mnc = Integer.valueOf(netOperator.substring(3, 5));  //移动网络号码，中国移动为0，联通为1，电信为2
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(cid);
        stringBuffer.append(',');
        stringBuffer.append(lac);
        stringBuffer.append(',');
        stringBuffer.append(mnc);
        netInfoText.setText(stringBuffer.toString());  
    }  

	/** 
	 * 返回手机移动数据的状态 
	 * @param pContext 
	 * @param arg 
	 *            默认填null 
	 * @return true 连接 false 未连接 
	 */
	public boolean getMobileDataState(){
		ConnectivityManager mCM = (ConnectivityManager) super.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class ownerClass = mCM.getClass();
		Class[] argsClass = null;
		Object[] argObject = null;
		Boolean isOpen = false;
		try{
			Method method = ownerClass.getMethod("getMobileDataEnabled",argsClass);
			isOpen = (Boolean) method.invoke(mCM, argObject);
		}catch (Exception e){			
			e.printStackTrace();
		}
		return isOpen;
	}
	
	public void setMoblieData(boolean enabled){
		try{
			ConnectivityManager mCM = (ConnectivityManager) super.
					getSystemService(Context.CONNECTIVITY_SERVICE);
			Class ownerClass = mCM.getClass();
			Class[] argsClass = new Class[1];
			argsClass[0] = boolean.class;
			Method method = ownerClass.getMethod("setMobileDataEnabled",argsClass);
			method.invoke(mCM, enabled);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private class ButtonListener01 implements OnClickListener{
		public void onClick(View arg0){	
			if(wifiManager.isWifiEnabled()){
				wifiManager.setWifiEnabled(false);				
				if(!getMobileDataState())
					setMoblieData(true);
			}
			else{
				if(getMobileDataState()){
					setMoblieData(false);				
				}
				if(!wifiManager.isWifiEnabled()){
					wifiManager.setWifiEnabled(true);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
