package com.openproject.xiaojie.android_ipprint;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.openproject.xiaojie.android_ipprint.print.PrintUtil;

import java.io.IOException;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PRINT_OPEN = 1;
    public static final int PRINT_SET = 2;
    public static final int PRINT_PRINT = 3;
    public static final int PRINT_CLOSE = 4;
    public static final int PRINT_STATUS = 5;

    private PrintUtil printUtil;
    private EditText ipAddress;
    private Button serIp;
    private String ip;
    private Button connectTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyToast.init(this);
        printUtil = new PrintUtil();
        ipAddress = (EditText) findViewById(R.id.ip_address);
        serIp = (Button) findViewById(R.id.set_ip);
        connectTest = (Button) findViewById(R.id.connect_test);
        serIp.setOnClickListener(this);
        connectTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_ip:
                ip = ipAddress.getText().toString();
                Toast.makeText(this, "ip设置成功!" + ip, Toast.LENGTH_SHORT).show();
                break;
            case R.id.connect_test: //连接打印机不能在主线程
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Looper.prepare();   //子线程中使用handler
                        try {
                            printUtil.open(ip);
                            printUtil.Set();
                            printUtil.printTextNewLine("打印测试");
                            printUtil.printQrCode("小熊快跑！").printEnter().printTextNewLine("ddddddddd").printEnter()
                            .getStatus2();
                            printUtil.printBarCode("1001111");
                            printUtil.CutPage(4);
                            printUtil.Close();
                        } catch (IOException e) {
                            Log.e("MainActivity", "run (93): IO错误",e);
                        }
                        Looper.loop();
                    }
                };
                thread.start();

                break;
        }
    }
}
