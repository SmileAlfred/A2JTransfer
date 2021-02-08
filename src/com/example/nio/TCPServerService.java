package com.example.nio;

import java.util.concurrent.*;

public class TCPServerService {
    private static final String TAG = TCPServerService.class.getSimpleName();

    public final static int SERVER_PORT = 9999;                     // 跟客户端绝定的端口

    private static TCPServer mTCPServer;
    private static ThreadPoolExecutor mConnectThreadPool;                  // 总的连接线程池

    public TCPServerService() {
        init();
        initTcpServer();
    }

    public static void main(String[] args) {

    }

    public void onDestroy() {
        unInitTcpServer();
    }

    private static void init() {
        mConnectThreadPool = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "server_thread_pool");
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        System.out.println(TAG + "已启动连接，请免重复操作");
                    }
                }
        );
    }

    /**
     * 初始化TCP服务
     */
    private static void initTcpServer() {
        mConnectThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mTCPServer = new TCPServer();
                mTCPServer.init();
            }
        });
    }

    /**
     * 反初始化TCP服务
     */
    private void unInitTcpServer() {
        mTCPServer.close();
    }
}
