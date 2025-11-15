package org.dochi.webserver.bootstrap;

public class ShutdownTasksManager {
    private static final ShutdownTasksManager INSTANCE = new ShutdownTasksManager();
    private final ShutdownTasksProcessor processor = new ShutdownTasksProcessor();

    private ShutdownTasksManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(processor::run, "shutdown-hook"));
    }

    public static ShutdownTasksManager getInstance() {
        return INSTANCE;
    }

    public void addShutdownHook(Runnable task) {
        processor.add(task);
    }

    public void removeShutdownHook(Runnable task) {
        processor.remove(task);
    }

    public int getShutdownHookCount() {
        return processor.count();
    }
}