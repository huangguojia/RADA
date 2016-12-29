package com.example.john.radar;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private List<Info> infoList = new ArrayList<Info>();
    private List<Info> infoList1 = new ArrayList<Info>();
    private SmsReceiver smsReceiver;
    private IntentFilter receiveFilter;
    private Button locate;
    private ToggleButton refresh;
    private Button friends;
    private Button enemies;
    private BaiduMap mBaiduMap;
    private BitmapDescriptor bitmap;
    private BitmapDescriptor bitmap1;
    MapView mMapView = null;
    public LocationClient mLocationClient = null;
    public OverlayOptions option;
    public OverlayOptions optionsName;
    public OverlayOptions optionsNum;
    public LatLng Pos;
    public LatLng NamePos;
    public LatLng NumPos;
    public Bundle extraMsg;
    boolean isFirstLoc = true;// 是否首次定位
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);    //设置定位数据

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);   //设置地图中心点以及缩放级别
                mBaiduMap.animateMapStatus(u);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //第一次登录preference的“firststart”为“true”
        preferences = getSharedPreferences("phone", Context.MODE_PRIVATE);
        Log.i("判断是否为第一次登录",preferences.getBoolean("firststart", true)+"");
        //判断是不是首次登录
        if (preferences.getBoolean("firststart", true)) {
            editor = preferences.edit();
            //将登录标志位设置为false，即将preference的“firststart”修改为“false”
            editor.putBoolean("firststart", false);
            //提交修改
            editor.commit();
            //第一次登录，建立空文件“friends”
            try{
                infoList.clear();
                saveObject("friends",infoList);
                infoList1.clear();
                saveObject("enemies",infoList1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onCreate(savedInstanceState);

//        //地图初始化
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        infoList = (List<Info>)getObject("friends");
        infoList1 = (List<Info>)getObject("enemies");
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        this.initLocation();
        mLocationClient.start();

        //定位按钮，点击重新定位
        locate = (Button)findViewById(R.id.btn_locate);
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng ll = new LatLng(mLocationClient.getLastKnownLocation().getLatitude(),
                        mLocationClient.getLastKnownLocation().getLongitude());
                double zoomLevel = mBaiduMap.getMapStatus().zoom;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll,(float)zoomLevel);   //设置地图中心点以及缩放级别
                mBaiduMap.animateMapStatus(u);
            }
        });

        //刷新按钮，点击发送短信
        refresh = (ToggleButton)findViewById(R.id.btn_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, new Intent(), 0);
                SmsManager sms = SmsManager.getDefault();
                for(Info x:infoList){
                    sms.sendTextMessage(x.getTele(), null, "where are you?", pi, null);
                }
                for(Info x:infoList1){
                    sms.sendTextMessage(x.getTele(), null, "where are you?", pi, null);
                }
            }
        });

        //朋友按钮，点击跳转朋友列表页面
        friends = (Button)findViewById(R.id.btn_friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Friend.class);
                startActivity(intent);
            }
        });
        //敌人按钮，点击跳转敌人列表页面
        enemies = (Button)findViewById(R.id.btn_enemies);
        enemies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Enemy.class);
                startActivity(intent);
            }
        });
        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = new SmsReceiver();
        registerReceiver(smsReceiver,receiveFilter);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = null;
                if(marker.getIcon().equals(bitmap)){
                    intent = new Intent(MainActivity.this,FriendDetail.class);
                }
                else if(marker.getIcon().equals(bitmap1)){
                    intent = new Intent(MainActivity.this,EnemyDetail.class);
                }
                String name = marker.getExtraInfo().getString("name");
                String number = marker.getExtraInfo().getString("number");
                String longitude = marker.getExtraInfo().getString("longitude");
                String latitude = marker.getExtraInfo().getString("latitude");
                intent.putExtra("name",name);
                intent.putExtra("number",number);
                intent.putExtra("longitude",longitude);
                intent.putExtra("latitude",latitude);
                startActivity(intent);
                return false;
            }
        });
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }
    class SmsReceiver extends BroadcastReceiver {
        String mobile;
        String content;
        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] pduses= (Object[])intent.getExtras().get("pdus");
            for(Object pdus: pduses) {
                byte[] pdusmessage = (byte[]) pdus;
                SmsMessage sms = SmsMessage.createFromPdu(pdusmessage);
                mobile = sms.getOriginatingAddress();//发送短信的手机号码
                content = sms.getMessageBody(); //短信内容
            }
            System.out.println("-----------手机收到新短息----------");
            System.out.println(mobile+" "+content);
            if(content.equals("where are you?")){
                PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, new Intent(), 0);
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(mobile, null, mLocationClient.getLastKnownLocation().getLatitude()+"/"+mLocationClient.getLastKnownLocation().getLongitude(), pi, null);
            }
            //判断短信内容格式是否是xxx.xxxx/yyy.yyyyy形式的经纬度
            String REGEX = "^[0-9]+(.[0-9]+)?[//][0-9]+(.[0-9]+)?$";
            Pattern p = Pattern.compile(REGEX);
            Matcher m = p.matcher(content); // 获取 matcher 对象
            if(m.matches()){
                Log.i("正确","匹配");
                String temp[] = content.split("/");
                String name = "";
                String number = "";
                String latitude = temp[0];
                String longitude = temp[1];
                Log.i("检查检查"+latitude,longitude);

                //定位到对象的位置
                Pos = new LatLng(Double.valueOf(latitude),Double.valueOf(longitude));
                //名字位置
                NamePos = new LatLng(Double.valueOf(latitude)-0.0001,Double.valueOf(longitude));
                //电话号码位置
                NumPos = new LatLng(Double.valueOf(latitude)-0.0011,Double.valueOf(longitude));
                extraMsg = new Bundle();
                extraMsg.putString("longitude",longitude);
                extraMsg.putString("latitude",latitude);
                boolean ifFriend = false;
                for(Info x:infoList){
                    if(mobile.contains(x.getTele())){
                        //获取地点的位置标志图案
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.friend_marker);
                        name = x.getName();
                        number = x.getTele();
                        extraMsg.putString("name", name);
                        extraMsg.putString("number", number);
                        option = new MarkerOptions()
                                .position(Pos)
                                .icon(bitmap)
                                .extraInfo(extraMsg);
                        mBaiduMap.addOverlay(option);
                        optionsName = new TextOptions()
                                .position(NamePos)
                                .text(name)
                                .fontColor(0xFF00FF00)
                                .fontSize(36)
                                .typeface(Typeface.DEFAULT_BOLD);
                        mBaiduMap.addOverlay(optionsName);
                        optionsNum = new TextOptions()
                                .position(NumPos)
                                .text(number)
                                .fontColor(0xFFFFFFFF)
                                .fontSize(24)
                                .typeface(Typeface.DEFAULT_BOLD);
                        mBaiduMap.addOverlay(optionsNum);
                        ifFriend = true;
                        break;
                    }
                }
                if(!ifFriend){
                    for(Info x:infoList1){
                        if(mobile.contains(x.getTele())){
                            //获取地点的位置标志图案
                            bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.enemy_marker);
                            name = x.getName();
                            number = x.getTele();
                            extraMsg.putString("name", name);
                            extraMsg.putString("number", number);
                            option = new MarkerOptions()
                                    .position(Pos)
                                    .icon(bitmap1)
                                    .extraInfo(extraMsg);
                            mBaiduMap.addOverlay(option);
                            optionsName = new TextOptions()
                                    .position(NamePos)
                                    .text(name)
                                    .fontColor(0xFFFF0000)
                                    .fontSize(36)
                                    .typeface(Typeface.DEFAULT_BOLD);
                            mBaiduMap.addOverlay(optionsName);
                            optionsNum = new TextOptions()
                                    .position(NumPos)
                                    .text(number)
                                    .fontColor(0xFFFFFFFF)
                                    .fontSize(24)
                                    .typeface(Typeface.DEFAULT_BOLD);
                            mBaiduMap.addOverlay(optionsNum);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000); // 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
    }

    //从文件中获得数据
    private Object getObject(String name) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = this.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //将数据重新存储到文件中
    private void saveObject(String name,List<Info> data) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = this.openFileOutput(name, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


