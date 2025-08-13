package org.dochi.webserver.socket;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.config.*;
import org.dochi.webserver.executor.WorkerPoolExecutor;

public class SocketTaskExecutorFactory {
    private static final SocketTaskExecutorFactory INSTANCE = new SocketTaskExecutorFactory();

    private SocketTaskExecutorFactory() {}

    public static SocketTaskExecutorFactory getInstance() {
        return INSTANCE;
    }

    public SocketTaskExecutor createExecutor(ServerConfig serverConfig) {
        HttpConfig httpConfig = createHttpConfig(serverConfig);
        HttpApiMapper httpApiMapper = new HttpApiMapper(serverConfig.getWebService());
        WorkerPoolExecutor workerExecutor = new WorkerPoolExecutor(serverConfig.getThreadPool());
        SocketTaskPool taskPool = createTaskPool(serverConfig, httpConfig, httpApiMapper);
        return new SocketTaskExecutor(workerExecutor, taskPool);
    }

    private HttpConfig createHttpConfig(ServerConfig serverConfig) {
        return new HttpConfig(
                new HttpReqConfig(serverConfig.getHttpReqAttribute()),
                new HttpResConfig(serverConfig.getHttpResAttribute())
        );
    }

    private SocketTaskPool createTaskPool(
            ServerConfig serverConfig,
            HttpConfig httpConfig,
            HttpApiMapper httpApiMapper) {
        return new SocketTaskPool(
                serverConfig.getThreadPool(),
                () -> new SocketTaskHandler(
                        new SocketWrapper(serverConfig.getKeepAlive()),
                        httpApiMapper,
                        httpConfig
                )
        );
    }
}
