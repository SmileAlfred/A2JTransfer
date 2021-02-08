package com.example.nio;

import com.example.nio.bean.MsgBean;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static com.example.nio.utils.MyUtils.bufSize;

/**
 * 参考文档：
 * https://blog.csdn.net/qq_21539671/article/details/98743397
 * https://blog.csdn.net/c_o_d_e_/article/details/113092095
 * Android网络编程(十四) 之 Socket与NIO: https://blog.csdn.net/lyz_zyx/article/details/104062815
 *
 */

public class NIOMain {
    private static TCPClient mTcpClient1;
    private static String ipAdress = "192.168.137.1";

    public static void main(String[] args) {
        TCPServerService tcpServerService = new TCPServerService();







       /* mTcpClient1 = new TCPClient("客户端A");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("这里是客户端：\n1：建立连接；\n2：发送消息；\n3：断开连接");
            String input = scanner.next();
            switch (input) {
                case "1":
                    System.out.println("连接谁？");
                    String msg = scanner.next();
                    if (null != msg || msg.length() != 0)
                        ipAdress = msg;
                    mTcpClient1.requestConnectTcp(ipAdress);
                    break;
                case "2":
                    System.out.println("你想说啥？");
                    msg = scanner.next();
                    mTcpClient1.sendMsg(msg);
                    break;
                case "3":
                    mTcpClient1.disconnectTcp();
                    break;
                case "exist":
                    return;
                default:
                    break;
            }
        }*/
    }

    @Test
    public void Test01() {
        String originStr = "\"SmileAlfred\"";
        String newStr = originStr.replaceAll("\"", "");
        System.out.println("原Str = " + originStr + " ; 新Str = " + newStr);

    }


    @Test
    public void Test02() {
        String str = "我爱中国共产党！I love CHINA";
        byte[] bytes = str.getBytes();
        System.out.println("str = " + str + " ; str.length = " + str.length() + " ; bytes.length = " + bytes.length);
        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            newBytes[i] = bytes[i];
        }
        System.out.println(new String(newBytes));
    }

    @Test
    public void Test03() {
        String[] strs = new String[5];
        System.out.println("原数组：" + Arrays.toString(strs));
        String[] no1 = {"我", "我", "我", "我", "我"};
        String[] no2 = {"你", "你", "你", "你"};
        strs = no1;
        System.out.println("no1 赋值后：" + Arrays.toString(strs));

        strs = no2;
        System.out.println("no2 赋值后：" + Arrays.toString(strs));
    }

    @Test
    public void test04() {
        System.out.println("".getBytes().length);

        byte[] intBytes = new byte[4];
        byte[] longBytes = new byte[8];
        byte[] fileNameBytes = new byte[0];
        byte[] buf = new byte[64];

        //发送命令
        intBytes = "FormatUtils.toLH(this.msg_code)".getBytes();
        System.arraycopy(intBytes, 0, buf, 0, 4);

        //发送文件名
        fileNameBytes = "fileName.getBytes()".getBytes();
        System.arraycopy(fileNameBytes, 0, buf, 4, 0);

        //发送文件大小
        longBytes = "FormatUtils.toLH(this.fileLength)".getBytes();
        System.arraycopy(longBytes, 0, buf, 4 + 0, 8);

        System.out.println(Arrays.toString(buf));
    }

    //分散读取
    @Test
    public void test05() throws Exception {
        String property = System.getProperty("user.dir");
        FileInputStream FileInputStream = new FileInputStream(property + "/ab.txt");
        FileChannel channel = FileInputStream.getChannel();
        ByteBuffer allocate1 = ByteBuffer.allocate(32);//改为8
        ByteBuffer allocate2 = ByteBuffer.allocate(80);//改为80
        channel.read(new ByteBuffer[]{allocate1, allocate2});
        allocate1.rewind();
        allocate2.rewind();
        String i = new String(allocate1.array()).toString();
        String string = new String(allocate2.array());
        System.out.println(1 + " : " + i + " ; " + i.length());
        System.out.println(2 + " : " + string);

        channel.close();

    }

    @Test
    public void Test06() {
        File file = new File("D:/test.jpg");
        long fileSize = file.length();
        System.out.println(file.getName() + " 的大小：" + fileSize);//308253 字节

    }

    @Test
    public void test07() {
        ByteBuffer byteBufContent;
        while (true) {
            byteBufContent = ByteBuffer.allocate(bufSize(1024 * 1024 * 1024));
            byteBufContent = ByteBuffer.allocate(bufSize(1024));
            byteBufContent = ByteBuffer.allocate(bufSize(1024 * 1024));
            System.out.println("创建了缓冲区： " + byteBufContent.capacity() + " ; 证明了，缓冲区采用上述方法创建不会有内存问题");
        }

    }

    @Test
    public void test08() {
        int[] test = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        System.out.println(test.length);
    }

    //测试 byte[] 和 long 的转换
    @Test
    public void test09() {

        long test = 500L;
        //byte[] bytes = FormatUtils.toLH(test);
        byte[] bytes = longToBytes(test);
        long res = bytesToLong(bytes);
        System.out.printf("源数据 ： %s ； 结果 ： %s", test, res);

    }

    //byte 数组与 long 的相互转换
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    //重命名
    @Test
    public void test10() {
        String name = "D:" + File.separator + "A2JTransfer" + File.separator + "fileName.jpg";
        String someThing = "_202110208";

        int i = name.lastIndexOf('.');
        StringBuffer stringBuffer = new StringBuffer(name);
        System.out.println(stringBuffer.insert(i, someThing).toString());
    }

    @Test
    public void test11(){
        byte[] bytes = {2, 0, 0, 0, 0, 0, 0, 0, 0, 56, 68, 91, 80, 49, 48, 50, 48, 55, 45, 49, 51, 48, 52, 53, 49, 46, 106, 112, 103, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        MsgBean struct = MsgBean.getStruct(bytes);
        System.out.println(struct);
    }
    @Test
    public void test12(){
        long res = -1L;
        System.out.println(res);
    }

    @Test
    public void test13(){

        int i = bufSize(3687515L);
        System.out.println(i);
    }

}
