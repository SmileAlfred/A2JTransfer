package com.example.nio.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author SmileAlfred
 * @create 2021-02-07 14:40
 * @csdn https://blog.csdn.net/liusaisaiV1
 * @description 一些工具类
 */
public class MyUtils {

    public static String filePath = "D:" + File.separator + "A2JTransfer" + File.separator;
    public static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd-HHmmss"),
            dateFormat = new SimpleDateFormat("yyyy-MM-dd"),
            timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * @param fileName 文件名 test.txt
     * @return D:/A2JTransfer/test.txt
     */
    public static String createFile(String fileName) {
        String name = "D:" + File.separator + "A2JTransfer" + File.separator + fileName;
        File testFile = new File(name);
        File fileParent = testFile.getParentFile();//返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs();// 能创建多级目录
        }
        if (!testFile.exists()) {
            try {
                testFile.createNewFile();//有路径才能创建文件
            } catch (IOException e) {
            }
        } else {
            //创建副本
            int i = name.lastIndexOf('.');
            name = insertStr(name, "_" + dateTimeFormat.format(new Date()), i);

            testFile = new File(name);
            try {
                testFile.createNewFile();//有路径才能创建文件
            } catch (IOException e) {
            }
        }
        return name;
    }

    /**
     * @param size B
     * @return 根据文件具体大小 返回对应的 缓存区大小
     */
    public static int bufSize(long size) {  //3687515
        long KB = (size / 1024);            //3,601
        long MB = KB / 1024;                //3.516
        long GB = MB / 1024;                //0
        if (GB > 0) return 1024 * 1024 * 1024;// ?GB
        if (MB > 0) return 1024 * 1024;// ?MB
        if (KB > 0) return 1024;
        return 1024 * 1024;
    }

    /**
     * 给 String 中插入字符
     *
     * @param src      原 str
     * @param des      待插入 Str
     * @param positoin 插入的位置，（插在该点之前）
     */
    public static String insertStr(String src, String des, int positoin) {
        StringBuffer stringBuffer = new StringBuffer(src);
        return stringBuffer.insert(positoin, des).toString();

    }
}