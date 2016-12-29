package com.example.john.radar;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Friend extends AppCompatActivity {
    private Button add;
    private Button delete;
    private Button ok;
    private Button close;
    private Button radar;
    private Button enemies;
    private EditText name;
    private EditText tele;
    private List<Info> infoList = new ArrayList<Info>();
    private ListView myListView;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list);
        myListView = (ListView)findViewById(R.id.lvw_friends_list);
        infoList = (List<Info>)getObject("friends");
        refresh();
        add = (Button) findViewById(R.id.btn_friends_list_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示添加好友对话框
                final Dialog dialog = new Dialog(Friend.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_add_friend);
                dialog.show();
                name = (EditText)dialog.findViewById(R.id.txt_friend_name);
                tele = (EditText)dialog.findViewById(R.id.txt_friend_number);
                ok = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //确认添加好友
                        Info friend = new Info();
                        friend.setName(name.getText().toString());
                        friend.setTele(tele.getText().toString());
                        infoList.add(friend);
                        saveObject("friends",infoList);
                        refresh();
                        dialog.dismiss();
                    }
                });
                close = (Button)dialog.findViewById(R.id.btn_dialog_close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //取消添加好友
                        dialog.dismiss();
                    }
                });
            }
        });
        radar = (Button)findViewById(R.id.btn_friends_list_radar);
        radar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Friend.this.finish();
            }
        });
        enemies = (Button)findViewById(R.id.btn_friends_list_enemies);
        enemies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Friend.this,Enemy.class);
                startActivity(intent);
                finish();
            }
        });
    }
    void refresh(){
        try {
            SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.friends_list_item, new String[]{"name"}, new int[]{R.id.name_cell}){
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    delete = (Button)v.findViewById(R.id.delete_button_cell);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //显示好友删除对话框
                            final Dialog dialog = new Dialog(Friend.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.dialog_delete);
                            dialog.show();
                            TextView txt_friend_name = (TextView)dialog.findViewById(R.id.txt_friend_name);
                            txt_friend_name.setText(infoList.get(position).getName());
                            TextView txt_friend_number = (TextView)dialog.findViewById(R.id.txt_friend_number);
                            txt_friend_number.setText(infoList.get(position).getTele());
                            ok = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //确认删除好友
                                    infoList.remove(position);
                                    saveObject("friends",infoList);
                                    refresh();
                                    dialog.dismiss();
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
                    return v;
                }
            };
            myListView.setAdapter(adapter);
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    //显示编辑好友对话框
                    final Dialog dialog = new Dialog(Friend.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_add_friend);
                    dialog.show();
                    name = (EditText)dialog.findViewById(R.id.txt_friend_name);
                    name.setText(infoList.get(position).getName());
                    tele = (EditText)dialog.findViewById(R.id.txt_friend_number);
                    tele.setText(infoList.get(position).getTele());
                    ok = (Button)dialog.findViewById(R.id.btn_dialog_ok);
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //确认编辑
                            infoList.get(position).setName(name.getText().toString());
                            infoList.get(position).setTele(tele.getText().toString());
                            saveObject("friends",infoList);
                            refresh();
                            dialog.dismiss();
                        }
                    });
                    close = (Button)dialog.findViewById(R.id.btn_dialog_close);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //取消编辑
                            dialog.dismiss();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //build a Map of String to Object,finally make them into a List
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Info it;
        int i=0;
        for(Iterator<Info> iter = infoList.iterator(); iter.hasNext();){
            i++;
            Map<String, Object> tmp = new HashMap<String, Object>();
            it = iter.next();
            tmp.put("name",it.getName());
            list.add(tmp);
        }
        return list;
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
