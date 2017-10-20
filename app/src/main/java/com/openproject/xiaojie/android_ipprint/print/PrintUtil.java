package com.openproject.xiaojie.android_ipprint.print;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;

/**
 * 打印工具类
 * Created by xxj on 07/31.
 */
public final class PrintUtil {

    private Socket socket;
    private int netPort = 9100;   //default 9100  0X238c
    private OutputStream stream;
    private String charSet = "GB18030";
//    private String charSet = "GB2312";

    private int Net_SendTimeout = 1000;
    private int ConnectTimeout = 20000;
    private int Net_ReceiveTimeout = 1000;
    private String command = ""; //打印命令字符串
    private byte[] outbytes; //传输的命令集

    PrinterCMD pcmd = new PrinterCMD();

    /**
     * 设置端口号，有默认值，除非自定义，否则不调用
     *
     * @param netPort 端口号
     */
    public PrintUtil netPort(int netPort) {
        this.netPort = netPort;
        return this;
    }

    /**
     * 设置打印字符集
     */
    public PrintUtil charSet(String charSet) {
        this.charSet = charSet;
        return this;
    }

    /**
     * 打开 Socket 连接
     *
     * @param ipaddress IP 地址
     */
    public PrintUtil open(String ipaddress) throws IOException {
        if (socket == null) {
            SocketAddress ipe = new InetSocketAddress(ipaddress, netPort);
            socket = new Socket();  //Socket(ipaddress, netPort,true);
            socket.connect(ipe, ConnectTimeout);
            socket.setSoTimeout(Net_ReceiveTimeout);
        } else {
            socket.close();
            SocketAddress ipe = new InetSocketAddress(ipaddress, netPort);
            socket = new Socket();  //Socket(ipaddress, netPort,true);
            socket.connect(ipe, Net_ReceiveTimeout);
            socket.setSoTimeout(Net_ReceiveTimeout);
        }
        Logger.e(ipaddress + "  IP 打印机连接成功！");
        stream = socket.getOutputStream();
        return this;
    }

    /**
     * 获取打印机状态
     */
    public String getStatus() throws IOException {
        InputStream stream = socket.getInputStream();
        byte[] bytes = new byte[4];
        stream.read(bytes);
        stream.close();
        socket.close();
        return bytes[0] + "," + bytes[1] + "," + bytes[2] + "," + bytes[3] + ",";
    }

    /**
     * 打印机关闭
     */
    public void Close() throws IOException {
        if (socket != null)
            socket.close();
        if (stream != null)
            stream.close();
        socket = null;
    }

    /**
     * 打印二维码
     *
     * @param qrData 二维码的内容
     * @throws IOException
     */
    protected void qrCode(String qrData) throws IOException {
        int moduleSize = 8;
        int length = qrData.getBytes(charSet).length;
        OutputStreamWriter writer = new OutputStreamWriter(stream, charSet);

        //打印二维码矩阵
        writer.write(0x1D);// init
        writer.write("(k");// adjust height of barcode
        writer.write(length + 3); // pl
        writer.write(0); // ph
        writer.write(49); // cn
        writer.write(80); // fn
        writer.write(48); //
        writer.write(qrData);

        writer.write(0x1D);
        writer.write("(k");
        writer.write(3);
        writer.write(0);
        writer.write(49);
        writer.write(69);
        writer.write(48);

        writer.write(0x1D);
        writer.write("(k");
        writer.write(3);
        writer.write(0);
        writer.write(49);
        writer.write(67);
        writer.write(moduleSize);

        writer.write(0x1D);
        writer.write("(k");
        writer.write(3); // pl
        writer.write(0); // ph
        writer.write(49); // cn
        writer.write(81); // fn
        writer.write(48); // m

        writer.flush();

    }

    /**
     * 网络打印机 初始化打印机
     */
    public PrintUtil Set() throws IOException {
        stream.write(pcmd.CMD_SetPos());
        return this;
    }

    /**
     * 设置行高
     *
     * @param n
     * @return
     */
    public PrintUtil lineHeight(int n) throws IOException {
        stream.write(pcmd.CMD_SetLineHeight(n));
        return this;
    }

    public PrintUtil setMargin(int x, int y) throws IOException {
        stream.write(pcmd.CMD_MarginSetting(x, y));
        return this;
    }

    public PrintUtil printTab() throws IOException {
        stream.write(pcmd.CMD_Tab());
        return this;
    }

    /**
     * 打印绝对位置的文字
     *
     * @param text
     * @param nl
     * @param nm
     * @return
     */
    public PrintUtil printAbsText(String text, int nl, int nm) throws IOException {
        stream.write(new byte[]{29, 84, 0});//GS
        stream.write(pcmd.CMD_AbsLocation(nl, nm));
        stream.write(text.getBytes(Charset.forName(charSet)));
        stream.flush();
        return this;
    }

    /**
     * 网络打印机 打印的文本
     *
     * @param pszString  要打印大的文本
     * @param nFontAlign 对齐方式    0:居左 1:居中 2:居右
     * @param nFontSize  字体大小    0:正常大小 1:两倍高 2:两倍宽 3:两倍大小 4:三倍高 5:三倍宽 6:三倍大小 7:四倍高 8:四倍宽 9:四倍大小 10:五倍高 11:五倍宽 12:五倍大小
     */
    public PrintUtil printText(String pszString, int nFontAlign, int nFontSize) throws IOException {
        stream.write(pcmd.CMD_TextAlign(nFontAlign));

        command = pcmd.CMD_FontSize(nFontSize);
        outbytes = command.getBytes(Charset.forName("ASCII"));
        stream.write(outbytes);

        command = pszString;// +CMD_Enter();
        outbytes = command.getBytes(Charset.forName(charSet)); //Charset.defaultCharset()); //forName("GB18030")
        stream.write(outbytes);
        stream.flush();
        return this;
    }

    public PrintUtil printText(String str) throws IOException {
        stream.write(str.getBytes(Charset.forName(charSet)));
        return this;
    }

    /**
     * 网络打印机 回车
     */
    public PrintUtil printEnter() throws IOException {
        stream.write(pcmd.CMD_Enter());
        return this;
    }

    /**
     * 网络打印机 切割
     *
     * @param pageNum 切割时，走纸行数
     */
    public PrintUtil CutPage(int pageNum) throws IOException {
        stream.write(pcmd.CMD_PageGO(pageNum));
        stream.write(pcmd.CMD_Enter());

        stream.write(pcmd.CMD_CutPage());
        stream.write(pcmd.CMD_Enter());
        return this;
    }

    /**
     * 网络打印机换行
     *
     * @param lines 行数
     */
    public PrintUtil PrintNewLines(int lines) throws IOException {
        command = pcmd.CMD_FontSize(0);
        outbytes = command.getBytes(Charset.forName("ASCII"));
        stream.write(outbytes);
        for (int i = 0; i < lines; i++) {
            stream.write(pcmd.CMD_Enter());
        }
        return this;
    }

    /**
     * 热敏打印指令集
     */
    @SuppressWarnings("unused")
    private class PrinterCMD {

        /**
         * 初始化打印机
         *
         * @return ESC @
         */
        public byte[] CMD_SetPos() {
            return new byte[]{27, 64};
        }

        /**
         * 换行（回车）根据当前行的行间距打印打印缓冲区的数据并走纸一行。
         * 无打印数据时只走纸1行
         *
         * @return LF
         */
        public byte[] CMD_Enter() {
            return new byte[]{10};
        }

        /**
         * 对齐模式
         *
         * @param n 0:左对齐 1:中对齐 2:右对齐
         * @return ESC a n
         */
        public byte[] CMD_TextAlign(int n) {
            return new byte[]{27, 97, (byte) n};
        }

        /**
         * 设定字符右边间隔
         *
         * @param n 0≤n≤255 设定字符右边的间隔，间隔为 n*0.125mm（n*0.0049”）
         * @return ESC (space) n
         */
        public byte[] CMD_TextRightMargin(int n) {
            return new byte[]{27, 32, (byte) n};
        }

        /**
         * 设置绝对打印位置 (一行只能设置一回)
         * 从行的开始到打印位置的距离是 [（nL+nH*256）*0.125mm]
         *
         * @param nl
         * @param nh
         * @return ESC $ nL nH
         */
        public byte[] CMD_AbsLocation(int nl, int nh) {
            return new byte[]{27, 36, (byte) nl, (byte) nh};
        }

        /**
         * 设置行距命令   默认 30
         * 设置行距（带一倍字高）为（n*0.125mm）所设参数带字高度，如果设置n≤18H（一倍字符高度）则按打印不空行处理，n≥18H（一倍字符高度）时多出的步数作为空行步数
         *
         * @param n
         * @return ESC 3 n
         */
        public byte[] CMD_SetLineHeight(int n) {
            return new byte[]{27, 51, (byte) n};
        }

        /**
         * 设置横向纵向的偏移单位值    25.4/x mm (1/x)英寸
         *
         * @param x 横向
         * @param y 纵向
         * @return xx
         */
        public byte[] CMD_MarginSetting(int x, int y) {
            return new byte[]{29, 80, (byte) x, (byte) y};
        }

        /// <summary>
        /// 字体的大小
        /// </summary>
        /// <param name="nfontsize">0:正常大小 1:两倍高 2:两倍宽 3:两倍大小 4:三倍高 5:三倍宽 6:三倍大小 7:四倍高 8:四倍宽 9:四倍大小 10:五倍高 11:五倍宽 12:五倍大小</param>
        /// <returns></returns>
        String CMD_FontSize(int nfontsize) {
            String _cmdstr = "";

            //设置字体大小
            switch (nfontsize) {
                case -1:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 0;//29 33
                    break;
                case 0:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 0;//29 33
                    break;

                case 1:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 1;
                    break;

                case 2:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 16;
                    break;

                case 3:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 17;
                    break;

                case 4:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 2;
                    break;

                case 5:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 32;
                    break;

                case 6:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 34;
                    break;

                case 7:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 3;
                    break;

                case 8:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 48;
                    break;

                case 9:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 51;
                    break;

                case 10:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 4;
                    break;

                case 11:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 64;
                    break;

                case 12:
                    _cmdstr = String.valueOf((char) 29) + (char) 33 + (char) 68;
                    break;

            }
            return _cmdstr;
        }

        /**
         * 走纸
         *
         * @param line 走纸的行数
         * @return ESC d
         */
        public byte[] CMD_PageGO(int line) {
            return new byte[]{27, 100, (byte) line};
        }

        /**
         * 制表
         *
         * @return HT
         */
        public byte[] CMD_Tab() {
            return new byte[]{9};
        }

        /**
         * 半切纸
         *
         * @return ESC m
         */
        public byte[] CMD_CutPage() {
            return new byte[]{27, 109};
        }

        /// <summary>
        /// 返回状态(返回8位的二进制)
        /// </summary>
        /// <param name="num">1:打印机状态 2:脱机状态 3:错误状态 4:传送纸状态</param>
        /// 返回打印机状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：0:一个或两个钱箱打开  1:两个钱箱都关闭
        /// 第四位：0:联机  1:脱机
        /// 第五位：固定为1
        /// 第六位：未定义
        /// 第七位：未定义
        /// 第八位：固定为0
        ///
        /// 返回脱机状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：0:上盖关  1:上盖开
        /// 第四位：0:未按走纸键  1:按下走纸键
        /// 第五位：固定为1
        /// 第六位：0:打印机不缺纸  1: 打印机缺纸
        /// 第七位：0:没有出错情况  1:有错误情况
        /// 第八位：固定为0
        ///
        /// 返回错误状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：未定义
        /// 第四位：0:切刀无错误  1:切刀有错误
        /// 第五位：固定为1
        /// 第六位：0:无不可恢复错误  1: 有不可恢复错误
        /// 第七位：0:打印头温度和电压正常  1:打印头温度或电压超出范围
        /// 第八位：固定为0
        ///
        /// 返回传送纸状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：0:有纸  1:纸将尽
        /// 第四位：0:有纸  1:纸将尽
        /// 第五位：固定为1
        /// 第六位：0:有纸  1:纸尽
        /// 第七位：0:有纸  1:纸尽
        /// 第八位：固定为0
        /// <returns></returns>

        public String CMD_ReturnStatus(int num) {
            return String.valueOf((char) 16) + (char) 4 + (char) num;
        }
    }

}
