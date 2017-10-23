package com.openproject.xiaojie.android_ipprint;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.openproject.xiaojie.android_ipprint.print.PrintTask;
import com.openproject.xiaojie.android_ipprint.print.PrintUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.openproject.xiaojie.android_ipprint.print.PrintTask.PRINT_COVER_PAPER_ERROR;
import static com.openproject.xiaojie.android_ipprint.print.PrintTask.PRINT_FAILURE;
import static com.openproject.xiaojie.android_ipprint.print.PrintTask.PRINT_OPEN_COVER;
import static com.openproject.xiaojie.android_ipprint.print.PrintTask.PRINT_PAPER_SHORT;
import static com.openproject.xiaojie.android_ipprint.print.PrintTask.PRINT_SUCCESS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PrintTask.PrintResult {

    private PrintUtil printUtil;
    private EditText ipAddress;
    private Button serIp;
    private String ip;
    private Button connectTest;
    private List<String> data;

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

        initData();
    }

    private void initData() {
        data = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            data.add("熊松考了第 " + (i + 1) + "名");
        }
    }

    private void printSync() {
        //提交到串行的线程池不是美滋滋？可以试下并行的，网口的打印机可能还好，WiFi 的。。呵呵哒
        new PrintTask(ip, this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_ip:
                ip = ipAddress.getText().toString();
                Toast.makeText(this, "ip设置成功!" + ip, Toast.LENGTH_SHORT).show();
                break;
            case R.id.connect_test: //连接打印机不能在主线程
                printSync();

//                printThread();

                break;
        }
    }

    private void printThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();   //子线程中使用handler
                try {
                    printUtil.open(ip);
                    printUtil.set();
                    printUtil.printTextNewLine("打印测试");
                    printUtil.printQrCode("小熊快跑！").printEnter().printTextNewLine("ddddddddd").printEnter()
                            .getStatus();
                    printUtil.printBarCode("1001111");
                    printUtil.CutPage(4);
                    printUtil.Close();
                } catch (IOException e) {
                    Log.e("MainActivity", "run (93): IO错误", e);
                }
                Looper.loop();
            }
        };
        thread.start();
    }

    @Override
    public void onPrintResult(int result) {
        switch (result) {
            case PRINT_SUCCESS:
                MyToast.showShort("打印成功！");
                break;
            case PRINT_FAILURE:
                showFailureDialog("打印失败，是否重试？");
                break;
            case PRINT_OPEN_COVER:
                showFailureDialog("打印机上盖开启，请关闭后点击重试按钮");
                break;
            case PRINT_PAPER_SHORT:
                showFailureDialog("打印机缺纸，请装纸后重试");
                break;
            case PRINT_COVER_PAPER_ERROR:
                showFailureDialog("打印机缺纸并且上盖打开，请装纸后重试");
                break;
            default:
                showFailureDialog("打印机未知状态，是否重新打印？");
                break;
        }
    }

    private void showFailureDialog(String msg) {
        new AlertDialog.Builder(this)
                .setTitle(msg)
                .setMessage("打印失败，是否重试？")
                .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        printSync();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create().show();
    }
}
