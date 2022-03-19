package com.snoopy.grpc.server.factory;

import com.snoopy.grpc.base.utils.LoggerBaseUtil;
import io.grpc.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * @author :   kehanjiang
 * @date :   2021/10/4  9:26
 */
public class GrpcServerLifecycle implements SmartLifecycle {
    private Server server;
    private NettyServerFactory factory;

    @Autowired
    public GrpcServerLifecycle(NettyServerFactory factory) {
        this.factory = factory;
    }

    /**
     * 启动netty服务
     */
    @Override
    public void start() {
        try {
            LoggerBaseUtil.info(this, "Initiating gRPC server startup");
            createAndStartGrpcServer();
            LoggerBaseUtil.info(this, "Completed gRPC server startup");
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to start the grpc server", e);
        }
    }

    @Override
    public void stop() {
        factory.removeAllRegistryServices();
        stopAndReleaseGrpcServer();
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isRunning() {
        Boolean isRunning = this.server != null && !this.server.isShutdown();
        LoggerBaseUtil.info(this, "gRPC server isRunning : " + isRunning);
        return isRunning;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }


    private void createAndStartGrpcServer() throws IOException {
        if (this.server == null) {
            this.server = this.factory.createServer();
            this.server.start();
            // Prevent the JVM from shutting down while the server is running
            Thread awaitThread = new Thread(() -> {
                try {
                    this.server.awaitTermination();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

    private void stopAndReleaseGrpcServer() {
        if (this.server != null) {
            LoggerBaseUtil.info(this, "Initiating gRPC server shutdown");
            this.server.shutdown();
            // Wait for the server to shutdown completely before continuing with destroying the spring context
            try {
                this.server.awaitTermination(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                this.server.shutdownNow();
                this.server = null;
            }
            LoggerBaseUtil.info(this, "Completed gRPC server shutdown");
        }
    }

}
