package com.example.networkmanager;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import android.os.Environment;
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
import GBDT.*;

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
	private int recordNum = 0;
	private Predictor predictor;
	private List<String> netInfoList = new ArrayList<String>();
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
		predictor = new Predictor();
		//app开启时训练GBDT model
		predictor.train();

		//获取按钮控件，设置事件监听
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new ButtonListener01());
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Runnable netRunnable = new Runnable(){
			public void run(){
				getLocationByGPS();
				getLocationByBaseStation();
				getCurrentTime();
				String currentInfo = dataToFile();
				double pred = 0.0;
				if(currentInfo != null){
					// 根据鞠策值来决定是否开启WiFi
					pred = predictor.predict(currentInfo);
					boolean WiFiEnable = false;
					if(pred > 0.5){
						WiFiEnable = true;
					}
					if(WiFiEnable){
						if(!wifiManager.isWifiEnabled()){
							wifiManager.setWifiEnabled(true);				
						}
					}
					info.setText("纬度：" + lastLatitude + "\n" + "经度：" + lastLongitude+ "\n" + "预测：" + pred);
				}
				handler.postDelayed(this,15000);//每15s获取一次位置信息和时间信息
			}
		};
		//判断GPS是否打开，
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//getLocationByGPS();
			handler.postDelayed(netRunnable,1000); 
		}else{
			Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
			handler.postDelayed(netRunnable,1000);
		}
		
	}
	public String dataToFile(){
		FileSave fs = new FileSave();
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
		fileText.setText(fileContent);
		int currentMinute = Integer.parseInt(fileContent.substring(14,16));
		if(currentContent.equals(lastContent) && Math.abs(currentMinute - lastMinute) < 5) return null;
		recordNum++;
		recordNumText.setText("记录数： "+String.valueOf(recordNum));
		netInfoList.add(fileContent);
		if(netInfoList.size() == 30){ //满30条记录后写入txt文件
			for(int i = 0;i<30;i++){
				String tempFileContent = netInfoList.get(i);
				fs.createExternalStoragePublicPicture(mContext, tempFileContent);
			}
			netInfoList = new ArrayList<String>();
		}
		lastContent = currentContent;
		lastMinute = currentMinute;
		return fileContent;
	}
	private int getNetworkStatus(){
		if(wifiManager.isWifiEnabled()){
			return 1;
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());	
		Calendar calendar = Calendar.getInstance();
		dateStr = format.format(new Date());
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
			}
			else{
				wifiManager.setWifiEnabled(true);
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
