package com.openproject.xiaojie.android_ipprint.print;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

/**
 * 这个用打印如果用多线程的方式有可能会出现指令并发导致串单的问题
 * 本来我是用的 RxJava 但是后来一想，写个 demo 而已，AsyncTask 自带串行线程池，没错，就用它了
 * Created by xxj on 10/23.
 */

public class PrintTask extends AsyncTask<List<String>, Void, Integer> {

    private String ipAddress;
    private PrintResult printResult;

    public static final int PRINT_SUCCESS = 0;
    public static final int PRINT_FAILURE = 1;
    public static final int PRINT_PAPER_SHORT = 3;
    public static final int PRINT_OPEN_COVER = 4;
    public static final int PRINT_UNDEFINE = 5;
    public static final int PRINT_COVER_PAPER_ERROR = 6;

    public PrintTask(String ipAddress, PrintResult printResult) {
        this.ipAddress = ipAddress;
        this.printResult = printResult;
    }


    @Override
    protected Integer doInBackground(List<String>[] lists) {
        PrintUtil printUtil = new PrintUtil();
        int status;
        try {
            printUtil.open(ipAddress).set();

            String printStatus = printUtil.getStatus();
            switch (printStatus) {
                case PrintUtil.PRINT_NORMAL:
                    status = PRINT_SUCCESS;
                    break;
                case PrintUtil.PRINT_PAPER_SHORT:
                    return PRINT_PAPER_SHORT;
                case PrintUtil.PRINT_OPEN_COVER:
                    return PRINT_OPEN_COVER;
                case PrintUtil.PRINT_COVER_PAPER_ERROR:
                    return PRINT_COVER_PAPER_ERROR;
                default:
                    status = PRINT_UNDEFINE;
                    break;
            }

            List<String> list = lists[0];
            for (String s : list) {
                printUtil.printTextNewLine(s);
            }
            printUtil.CutPage(8);
        } catch (IOException e) {
            return PRINT_FAILURE;
        } finally {
            try {
                printUtil.Close();
            } catch (IOException ignored) {
            }
        }
        return status;
    }

    private int getStatus(PrintUtil printUtil) throws IOException {
        int status;
        String printStatus = printUtil.getStatus();
        switch (printStatus) {
            case PrintUtil.PRINT_NORMAL:
                status = PRINT_SUCCESS;
                break;
            case PrintUtil.PRINT_PAPER_SHORT:
                status = PRINT_PAPER_SHORT;
                break;
            case PrintUtil.PRINT_OPEN_COVER:
                status = PRINT_OPEN_COVER;
                break;
            default:
                status = PRINT_UNDEFINE;
                break;
        }
        return status;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        printResult.onPrintResult(integer);
    }

    public interface PrintResult {
        void onPrintResult(int result);
    }
}
