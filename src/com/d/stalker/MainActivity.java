package com.d.stalker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.d.stalker.bean.Location;
import com.d.stalker.bean.Message;

public class MainActivity extends Activity {

	MinaClient client;
	private LocationClient mLocClient;
	private Context context;
	private BDLocation currentLocation;
	private SDKReceiver mReceiver;
	private TextView message;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		message=(TextView)findViewById(R.id.content);
		context=this;
		client=new MinaClient(this);
		iniLocation();
		new Thread(client).start();
	}
	@Override
	protected void onDestroy() {
		mLocClient.stop();
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	public void send(View v){
		EditText et=(EditText)findViewById(R.id.message);
		Message  message=new Message();
		message.setToUser("user2");
		message.setMessage(et.getText().toString());
		client.sendMessage(message);
	}
	public void loc(View v){
		
		mLocClient.start();
	}
	public void closeC(View v){
		client.close();
	}
	public void iniLocation(){
		
		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(new MyLocationListenner());
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//���ö�λģʽ
		option.setCoorType("bd09ll");//���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
		int span=1000*60;
		option.setOpenGps(true);
		option.setScanSpan(span);
		option.setIsNeedAddress(true);
		mLocClient.setLocOption(option);
		
		// ע�� SDK �㲥������
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
	    mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
	}
	
	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			message.setText("��ǰλ�ã�"+location.getAddrStr());
			
			currentLocation=location;
			Message  message=new Message();
			message.setToUser("user2");
			message.setCmd("location");
			message.setLocation(Location.toLocation(location));
			client.sendMessage(message);
			System.out.println("���ȣ�"+location.getLatitude()+",ά�ȣ�"+location.getAltitude());
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	/**
	 * ����㲥�����࣬���� SDK key ��֤�Լ������쳣�㲥
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			message.setTextColor(Color.RED);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				message.setText("key ��֤����! ���� AndroidManifest.xml �ļ��м�� key ����");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				message.setText("�������");
			}
		}
	}
}
