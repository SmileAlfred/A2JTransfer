package com.example.nio;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;

/**
 * @author SmileAlfred
 * @create 2021-02-11 8:31
 * @csdn https://blog.csdn.net/liusaisaiV1
 * @description 图形化界面测试 Frame
 */
public class TestFrame {
    /**
     * @param args
     * @Title : main
     * @Type : Frame
     * @date : 2014年4月5日 下午7:09:29
     * @Description :
     */
    public static void main(String[] args) {
        Frame frame = new Frame("Windows");
        /**
         * 创建一个Panel容器
         */
        Panel panel = new Panel();
        /**
         * 向容器添加两个组件
         */
        panel.add(new TextField(30));
        panel.add(new Button("Send"));
        /**
         * 将Panel容器添加到Frame窗口中
         */
        frame.add(panel);
        /**
         * 设置窗口大小、位置
         */
        frame.setBounds(50, 50, 400, 400);
        /**
         * 将窗口显示出来
         */
        frame.setVisible(false);


        JLabel lblNewLabel_1 = new JLabel("");

        JFileChooser jf = new JFileChooser();
        jf.setDialogTitle("选择头像");
        jf.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File dir) {

                String name = dir.getName();
                if (dir.isDirectory() || name.endsWith("jpg") || name.endsWith("gif") || name.endsWith("png"))
                    return true;
                else
                    return false;
            }

            @Override
            public String getDescription() {
                // TODO Auto-generated method stub
                return null;
            }
        });


        int result = jf.showOpenDialog(frame);
        jf.setVisible(true);
        File selectedFile = null;//选择的文件
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = jf.getSelectedFile();
            //文件存在
            if (selectedFile.exists()) {

                String fliepath = selectedFile.getPath();
                ImageIcon iic = new ImageIcon(fliepath);
                iic.setImage(iic.getImage().getScaledInstance(lblNewLabel_1.getWidth(), lblNewLabel_1.getHeight(), Image.SCALE_DEFAULT));
                lblNewLabel_1.setIcon(iic);

            }
        }


    }


}
