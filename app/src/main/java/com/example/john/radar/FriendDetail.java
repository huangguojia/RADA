package com.example.john.radar;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FriendDetail extends AppCompatActivity {
    TextView txt_friend_name;
    TextView txt_friend_number;
    TextView txt_friend_long_lang;
    TextView txt_friend_altitude;
    TextView txt_friend_accuracy;
    TextView txt_friend_nearest_city;
    TextView txt_friend_secs_last_update;
    TextView txt_friend_secs_next_update;

    private Button list;
    private Button radar;
    private Button delete;
    private Button ok;
    private Button close;

    String name;
    String number;
    String longitude;
    String latitude;

    private List<Info> infoList = new ArrayList<Info>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_detail);
        txt_friend_name = (TextView)findViewById(R.id.txt_friend_name);
        txt_friend_number = (TextView)findViewById(R.id.txt_friend_number);
        txt_friend_long_lang = (TextView)findViewById(R.id.txt_friend_long_lang);
        txt_friend_altitude = (TextView)findViewById(R.id.txt_friend_altitude);
        txt_friend_accuracy = (TextView)findViewById(R.id.txt_friend_accuracy);
        txt_friend_nearest_city = (TextView)findViewById(R.id.txt_friend_nearest_city);
        txt_friend_secs_last_update = (TextView)findViewById(R.id.txt_friend_secs_last_update);
        txt_friend_secs_next_update = (TextView)findViewById(R.id.txt_friend_secs_next_update);
        name = (String)this.getIntent().getExtras().get("name");
        number = (String)this.getIntent().getExtras().get("number");
        longitude = (String)this.getIntent().getExtras().get("longitude");
        latitude = (String)this.getIntent().getExtras().get("latitude");

        txt_friend_name.setText(name);
        txt_friend_number.setText(number);
        txt_friend_long_lang.setText(latitude+"/"+longitude);

        list = (Button)findViewById(R.id.btn_friends_list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendDetail.this,Friend.class);
                startActivity(intent);
                finish();
            }
        });

        radar = (Button)findViewById(R.id.btn_radar);
        radar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendDetail.this.finish();
            }
        });
        infoList = (List<Info>)getObject("friends");
        delete = (Button)findViewById(R.id.btn_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示好友删除对话框
                final Dialog dialog = new Dialog(FriendDetail.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.show();
                TextView txt_friend_name = (TextView)dialog.findViewById(R.id.txt_friend_name);
                txt_friend_name.setText(name);
                TextView txt_friend_number = (TextView)dialog.findViewById(R.id.txt_friend_number);
                txt_friend_number.setText(number);
                ok = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //确认删除好友
                        Iterator<Info> iter = infoList.iterator();
                        while(iter.hasNext()){
                            Info s = iter.next();
                            if(s.getName().equals(name) && s.getTele().equals(number)){
                                iter.remove();
                                break;
                            }
                        }
                        saveObject("friends",infoList);
                        dialog.dismiss();
                        Intent intent = new Intent(FriendDetail.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                close = (Button)dialog.findViewById(R.id.btn_dialog_close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //取消删除好友
                        dialog.dismiss();
                    }
                });
            }
        });
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
