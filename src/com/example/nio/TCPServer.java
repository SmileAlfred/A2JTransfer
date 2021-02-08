package com.example.nio;

import com.example.nio.bean.MsgBean;
import com.example.nio.utils.MyUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer {
    private final static String TAG = TCPServer.class.getSimpleName();
    private Scanner scanner;

    private Selector mSelector;
    private byte[] msgBytes;
    private List<byte[]> msgBytesList;
    private ByteBuffer byteBufContent, byteBufMsg, sendBuffer;


    private String fileWholeName;
    private boolean transfering = false;

    public void init() {
        msgBytesList = new ArrayList<>();

        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 设置非阻塞
            serverSocketChannel.configureBlocking(false);
            // 获取与此Channel关联的ServerSocket并绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(9999));
            // 注册到Selector，等待连接
            mSelector = Selector.open();
            serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器已启动：");

            while (mSelector != null && mSelector.isOpen()) {
                // 选择一组对应Channel已准备好进行I/O的Key
                int select = mSelector.select();
                if (select <= 0) {
                    continue;
                }
                // 获得Selector已选择的Keys
                Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    // 移除当前的key
                    iterator.remove();

                    if (selectionKey.isValid() && selectionKey.isAcceptable()) {
                        handleAccept(selectionKey);
                    }
                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                        handleRead(selectionKey);
                    }
                    if (selectionKey.isValid() && selectionKey.isWritable()) {
                        handleWrite(selectionKey);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mSelector != null) {
                    mSelector.close();
                    mSelector = null;
                }
                if (serverSocketChannel != null) {
                    serverSocketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    SocketChannel mClient;

    private void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
        mClient = server.accept();
        mClient.configureBlocking(false);
        // 注册读就绪事件
        mClient.register(mSelector, SelectionKey.OP_READ);
        System.out.println(TAG + "服务端 同意 客户端(" + mClient.getRemoteAddress() + ") 的连接请求");
    }

    //不应为每次读取分配一次ByteBuffer
    private void handleRead(SelectionKey selectionKey) throws Exception {
        if (transfering) return;
        SocketChannel client = (SocketChannel) selectionKey.channel();

        if (null == byteBufMsg) byteBufMsg = ByteBuffer.allocate(64);
        //读取服务器发送来的数据到缓冲区中
        int bytesRead = client.read(byteBufMsg);
        //发送来的消息 非空，那么读取
        if (bytesRead > 0) {
            //对消息进行处理，若发来的是文件，那么返回文件大小（byte）
            long fileLength = handleReceivedMsg(selectionKey, byteBufMsg.array());
            //如果文件大小 > 0 ,那么就是开辟一个新的缓存区，对文件进行接收
            if (fileLength > 0) {
                transfering = true;
                int bufferSize = (int) MyUtils.bufSize(fileLength);
                if (null == byteBufContent || bufferSize != byteBufContent.capacity())
                    byteBufContent = ByteBuffer.allocate(bufferSize);

                FileChannel fileChannel = new FileOutputStream(fileWholeName).getChannel();

                int writed = 0;
                int len = -1;
                //将客户端写入通道的数据读取并存储到buffer中
                while (writed < fileLength) {
                    //while ((len = client.read(byteBufContent)) > 0) {
                    len = client.read(byteBufContent);
                    //这里睡 10ms 很关键！这里是多线程的问题；TODO:多线程解决这个愚蠢的解决方式……
                    if (len <= 0) {
                        Thread.sleep(5);
                        continue;
                    }

                    writed += len;
                    //System.out.println(writed + " / "+fileLength + " B");
                    byteBufContent.flip();//将缓冲区翻转为读模式

                    fileChannel.write(byteBufContent);
                    byteBufContent.clear();//清除本次缓存区内容

                }
                System.out.println(MyUtils.timeFormat.format(new Date()) + ":文件接收完成");

                fileChannel.close();
                transfering = false;
                //TODO:回收 byteBufContent
            
           /* String inMsg = new String(byteBuffer.array(), 0, bytesRead);
            if ("Hello".equals(inMsg)) {
                sendMsg(selectionKey, inMsg);
                return;
            }
            // 处理数据
            //responseMsg(selectionKey, inMsg);
            try {
                receiveFile(selectionKey);
            } catch (Exception e) {
            }*/
            }
        }
    }

    private void handleWrite(SelectionKey selectionKey) throws IOException {
        if (null == msgBytes) {
            return;
        }

        SocketChannel client = (SocketChannel) selectionKey.channel();

        if (null == sendBuffer) sendBuffer = ByteBuffer.allocate(64);
        //sendBuffer.put(mSendMsg.getBytes());
        for (int i = 0; i < msgBytesList.size(); i++) {
            sendBuffer.put(msgBytesList.get(0));
            sendBuffer.flip();
            msgBytesList.remove(0);
        }


        client.write(sendBuffer);
        sendBuffer.clear();
        msgBytes = null;
        //mClient.register(mSelector, SelectionKey.OP_READ);
    }

    private long handleReceivedMsg(SelectionKey selectionKey, byte[] array) throws Exception {
        MsgBean msgBeanReceived = MsgBean.getStruct(array);
        System.out.println("服务器收到MSG：" + msgBeanReceived);
        MsgBean msgBean4Send;
        long fileLength = -1L;
        int msg_code = msgBeanReceived.getMsg_code();
        switch (msg_code) {
            case MsgBean.ORDER_REQUEST_IS_RECEIVE://请求是否接收文件？
                //Test scanner = new Scanner(System.in);
                String name = msgBeanReceived.getFileName();
                //Test System.out.print("是否接收文件 " + name + "？(Y/N) ");
                //Test String input = scanner.next();
                //Test switch (input) {
                //Test     case "Y":

                msgBean4Send = new MsgBean(MsgBean.ORDER_ALLOW_SEND, "", 0);
                sendMsg(selectionKey, msgBean4Send);
                fileWholeName = MyUtils.createFile(name);
                fileLength = msgBeanReceived.getFileLength();
                System.out.println(MyUtils.timeFormat.format(new Date()) + "服务器：我同意接收文件：" + name + " ;文件大小为：" + fileLength + " B");

                //Test          break;
                //Test      case "N":
                //Test          msgBean4Send = new MsgBean(MsgBean.ORDER_REJECT_SEND, "", 0);
                //Test          sendMsg(selectionKey, msgBean4Send);
                //Test          break;
                //Test      default:
                //Test          scanner.close();
                //Test          break;
                //Test  }
                break;

            case MsgBean.ORDER_ALLOW_SEND://允许接收文件
                System.out.println("服务器：这就发送文件去！");
                //String path = "D:/OneDrive/图片/本机照片/test.txt";
                //sendFile(selectionKey, path);
                break;
            case MsgBean.ORDER_REJECT_SEND://拒绝接收文件
                break;
            case MsgBean.ORDER_SEND_STR://谨发送消息
                System.out.println("Client:" + msgBeanReceived.fileName);
                if ("Test".equals(msgBeanReceived.fileName)) {
                    msgBean4Send = new MsgBean(MsgBean.ORDER_REQUEST_IS_RECEIVE, "test.txt", 8);
                    sendMsg(selectionKey, msgBean4Send);

                    Thread.sleep(2000);
                    System.out.println("服务器：这就发送文件去！");
                    String path = "D:/OneDrive/图片/本机照片/test.txt";
                    sendFile(selectionKey, path);
                }

               /* if (null == scanner) scanner = new Scanner(System.in);
                System.out.print("Server:");
                String input = scanner.next();
                if ("..".equals(input)) break;
                msgBean4Send = new MsgBean(MsgBean.ORDER_SEND_STR, input, 0);
                sendMsg(selectionKey, msgBean4Send);*/
                break;
            case MsgBean.ORDER_SUCESS__MSG://接收成功
                break;
            default:
                break;
        }
        return fileLength;
    }

    /**
     * 发送数据;        BUG:当连续发送消息，而文件没有及时取出时！消息将被覆盖！
     *
     * @param selectionKey
     * @param msg
     * @throws IOException
     */
    public void sendMsg(SelectionKey selectionKey, MsgBean msg) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        //mSendMsg = msg;
        msgBytes = msg.getBuf();

        msgBytesList.add(msgBytes);
        client.register(mSelector, SelectionKey.OP_WRITE);
        mSelector.wakeup();
        System.out.println(TAG + "服务端 给 客户端" + " 发送数据：" + msg + selectionKey.isReadable() + " ; " + selectionKey.isWritable());
    }

    /**
     * 处理数据
     *
     * @param selectionKey
     * @param inMsg
     * @throws IOException
     */
    private void responseMsg(SelectionKey selectionKey, String inMsg) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        System.out.println(TAG + "服务端 收到 客户端(" + client.getRemoteAddress() + ") 数据：" + inMsg.substring(0, 6));

        // 估计1亿的AI代码
        //String outMsg = inMsg;
        //outMsg = outMsg.replace("吗", "");
        //outMsg = outMsg.replace("?", "!");
        //outMsg = outMsg.replace("？", "!");


        //if (null == scanner) scanner = new Scanner(System.in);
        //System.out.print("发送：");
        //String msg = scanner.next();

        //sendMsg(selectionKey, msg);
    }

    //接受文件
    public static void mReceiveFile(SelectionKey selectionKey) throws Exception {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        long time = System.currentTimeMillis();//用时间戳命名【系统处理速度很快，时间戳命名，有可能造成文件覆盖，即不足一毫秒就处理完成】

        String fileWholeName = MyUtils.createFile("time.jpg");
        while (true) {
          /*  SocketChannel ac = ssc.accept();
            if (ac != null) {
                ac.configureBlocking(false);
                list.add(ac);//将个客户端添加到集合中
                System.out.println(ac);
            }
            for (SocketChannel soc : list) {*/
            FileOutputStream fos = null;
            FileChannel fch = null;

            /*****获取文件大小 *******/
            ByteBuffer cc = ByteBuffer.allocateDirect(10240);
            long len = 0;
            if (client.read(cc) > 0) {

                fos = new FileOutputStream(fileWholeName);
                fch = fos.getChannel();
                cc.flip();
                len = cc.getLong();
                System.err.println(len);
                cc.clear();
            }

            /*****获取文件大小    *******/

            /*****获取文件流 *******/
            reddd(client, len, 10240, fos, fch);
            //}
        }
    }

    //将客户端传递的数据封装为一个对象
    static class FileData {
        String clientAddress;
        String filename; //客户端上传的文件名称
        FileChannel fileOutChannel;//输出的文件通道
    }

    private void sendFile(SelectionKey selectionKey, String path) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        FileChannel fileChannel = fis.getChannel();
        //fileChannel.size() ==  file.length()
        long size = fileChannel.size();
        SocketChannel client = (SocketChannel) selectionKey.channel();

        ByteBuffer byteBufFile = ByteBuffer.allocateDirect(MyUtils.bufSize(size));

        int len = -1;
        int writed = 0;
        while (writed < size) {
            len = fileChannel.read(byteBufFile);
            writed += len;
            byteBufFile.flip();
            while (byteBufFile.hasRemaining()) {//保证字节全部写入

                client.write(byteBufFile);
            }
            byteBufFile.clear();
            System.out.println("已发送：" + writed);
        }
        fileChannel.close();
        fis.close();

        System.out.println("已发送：" + path);
    }

    //使用Map保存每个客户端传输，当OP_READ通道可读时，根据channel找到对应的对象
    Map<SelectableChannel, FileData> map = new ConcurrentHashMap<>();

    private final Charset charset = Charset.forName("UTF-8");

    /**
     * 接收文件
     */
    private void receiveFile(SelectionKey key) {
        String fileWholeName = MyUtils.createFile("time.jpg");

        ByteBuffer buffer = ByteBuffer.allocate(1024);//开启内存缓冲区域
        //FileData fileData = map.get(key.channel());

        SocketChannel socketChannel = (SocketChannel) key.channel();
        String directory = MyUtils.filePath;//服务端收到文件的存储路径
        long start = System.currentTimeMillis();

        try {
            FileChannel fileChannel = new FileOutputStream(fileWholeName).getChannel();
            //fileData.fileOutChannel = fileChannel;//赋值给client对象
            int len = 0;
            while ((len = socketChannel.read(buffer)) != -1) {//将客户端写入通道的数据读取并存储到buffer中
                buffer.flip();//将缓冲区翻转为读模式

                //客户端发送过来的，首先是文件名
               /* if (null == fileData.filename) {
                    // 文件名 decode解码为UTF-8格式，并赋值给client对象的filename属性
                    fileData.filename = (System.currentTimeMillis()+"_"+charset.decode(buffer).toString()).substring(5);

                    //先检查存储的目录是否存在
                    File dir = new File(directory);
                    if(!dir.exists()) dir.mkdir();

                    //再检查文件是否存在，不存在就创建文件，然后通过FikeChanel写入数据
                    File file = new File(directory + fileData.filename);
                    if(!file.exists()) file.createNewFile();

                    //将设定要存放的文件路径+文件名创建一个输出流通道
                    FileChannel fileChannel = new FileOutputStream(file).getChannel();
                    fileData.fileOutChannel = fileChannel;//赋值给client对象
                }
                //客户端发送过来的，最后是文件内容
                else{*/
                // 通过已经创建的文件输出流通道向文件中写入数据
                //fileData.fileOutChannel.write(buffer);
                fileChannel.write(buffer);
                //}
                buffer.clear();//清除本次缓存区内容
            }
            //fileData.fileOutChannel.close();
            fileChannel.close();
            key.cancel();

        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return;
        }

    }


    public static void reddd(SocketChannel soc, long len, int capacity, FileOutputStream fos, FileChannel fch) throws
            Exception {
        ByteBuffer bb = ByteBuffer.allocate(capacity);
        int read;
        while ((read = soc.read(bb)) > 0) {
            bb.flip();
            while (bb.hasRemaining()) {
                fch.write(bb);
            }
            fos.flush();
            Thread.sleep(1);//休眠一毫秒，这是关键代码，不可省略【应该有可替代方案】,休眠时间长短与客户端逻辑处理有关
            bb.clear();

            len -= read;
            if (len <= 0) {
                return;//结束当前方法
            }
            if (len < capacity) {//最后一次读取，数据少于容量值，按剩余长度再读取一次
                reddd(soc, len, (int) len, fos, fch);
                return;//结束当前方法
            }
        }
    }

    /**
     * 断开连接
     */
    public void close() {
        try {
            System.out.println(TAG + "服务端中断所有连接");
            mSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
