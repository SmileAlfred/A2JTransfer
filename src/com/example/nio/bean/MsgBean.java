package com.example.nio.bean;


import com.example.nio.utils.FormatUtils;

/**
 * @author: LiuSaiSai
 * @date: 2020/09/17 15:49
 * @description: 使用 对象 发送信息
 */
public class MsgBean {

    private static final String TAG = MsgBean.class.getSimpleName();

    public byte[] buf;
    //首位的各种命令 ORDER
    public static final int TEMP = 0,

    //握手
    ORDER_REQUEST_CONNECTION = 1,

    //请求是否接收文件
    ORDER_REQUEST_IS_RECEIVE = 2,
    //允许接收文件
    ORDER_ALLOW_SEND = 3,
    //拒绝接收文件
    ORDER_REJECT_SEND = 4,
    //谨发送消息
    ORDER_SEND_STR = 5,

    //接收成功
    ORDER_SUCESS__MSG = 6;


    //属性：命令消息
    public int msg_code;

    //接收 文件 的长度占 8 个字节；
    public long fileLength;

    //文件名（或聊天的消息）
    public String fileName;
    //传递的 文件名 的长度；（或发送消息的长度）
    public int fileNameLength;

    public MsgBean() {
    }

    /**
     * 发送结构体 时的构造器
     */
    public MsgBean(int msg_code, String fileName, long fileLength) {
        this.msg_code = msg_code;
        this.fileName = fileName;
        this.fileLength = fileLength;


        byte[] intBytes = new byte[4];
        byte[] longBytes = new byte[8];
        this.buf = new byte[64];

        //发送命令
        intBytes = FormatUtils.int2Bytes(this.msg_code);
        System.arraycopy(intBytes, 0, buf, 0, 4);

        //发送文件大小
        longBytes = FormatUtils.long2Bytes(this.fileLength);
        System.arraycopy(longBytes, 0, buf, 4 , 8);

        //发送文件名
        byte[]  fileNameBytes = fileName.getBytes();
        System.arraycopy(fileNameBytes, 0, buf, 12, fileNameBytes.length);

        this.fileNameLength = fileName.getBytes().length;
    }


    /**
     * 对接受的结构体数据进行解析
     *
     * @param buffer 结构体的字节流
     */
    public static MsgBean getStruct(byte[] buffer) {
        MsgBean msgBean = new MsgBean();
        byte[] intBytes = new byte[4];
        byte[] longBytes = new byte[8];
        int others = buffer.length - 12;
        byte[] fileNameBytes = new byte[others];

        //获取命令
        System.arraycopy(buffer, 0, intBytes, 0, 4);
        msgBean.setMsg_code(FormatUtils.byteArrayToInt(intBytes));

        //获取文件大小
        System.arraycopy(buffer, 4 , longBytes, 0, 8);
        msgBean.setFileLength(FormatUtils.bytes2Long(longBytes));

        //获取文件名
        System.arraycopy(buffer, 12, fileNameBytes, 0, others);
        msgBean.setFileName(new String(fileNameBytes).trim());

        //获取文件 名长度
        msgBean.setFileNameLength(msgBean.fileName .getBytes().length);

        return msgBean;
    }


    /**
     * 返回要发送的数组
     */
    public byte[] getBuf() {
        return buf;
    }

    public int getMsg_code() {
        return msg_code;
    }

    public void setMsg_code(int msg_code) {
        this.msg_code = msg_code;
    }

    public int getFileNameLength() {
        return fileNameLength;
    }

    public void setFileNameLength(int fileNameLength) {
        this.fileNameLength = fileNameLength;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "MsgBean{" +
                "msg_code=" + msg_code +
                ", fileLength=" + fileLength +
                ", fileName='" + fileName + '\'' +
                ", fileNameLength=" + fileNameLength +
                '}';
    }
}