package com.zbj.servicecommunicate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by zbj on 2017/3/24.
 */

public class MyActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int MSG_SUM = 0x110;

    private Button mBtnAdd;
    private LinearLayout mLyContainer;
    //显示连接状态
    private TextView mTvState;

    private Messenger mService;
    private boolean isConn;


    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msgFromServer) {
            switch (msgFromServer.what) {
                case 11:
                    TextView tv = (TextView) mLyContainer.findViewById(msgFromServer.arg1);
                    tv.setText(tv.getText() + "=>" + msgFromServer.arg2);
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });


    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //客户端通过IBinder获取服务端的messenger信使
            mService = new Messenger(service);
            isConn = true;
            mTvState.setText("connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isConn = false;
            mTvState.setText("disconnected!");
        }
    };

    private int mA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_mylayout);

        //开始绑定服务
        bindServiceInvoked();

        mTvState = (TextView) findViewById(R.id.id_tv_callback);
        mBtnAdd = (Button) findViewById(R.id.id_btn_add);
        mLyContainer = (LinearLayout) findViewById(R.id.id_ll_container);

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int a = mA++;
                    int b = (int) (Math.random() * 100);

                    //创建一个tv,添加到LinearLayout中
                    TextView tv = new TextView(MyActivity.this);
                    tv.setText(a + " + " + b + " = caculating ...");
                    tv.setId(a);
                    mLyContainer.addView(tv);
                    Message msgFromClient = Message.obtain(null, MSG_SUM, a, b);
                    //客户端把自己的信使传递给服务端（就出游了客户端的messenger对象从而可以操作客户端）
                    msgFromClient.replyTo = mMessenger;
                    if (isConn) {
                        //往服务端发送消息
                        mService.send(msgFromClient);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void bindServiceInvoked() {
        Intent intent = new Intent(this, MessengerService.class);
//        intent.setAction("com.zbj.servicecommunicate");
        //绑定服务绑定服务后,会调用 Connection 的 onServiceConnected 方法,
        bindService(intent, mConn, BIND_AUTO_CREATE);
        Log.e(TAG, "bindService invoked !");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
    }


}


