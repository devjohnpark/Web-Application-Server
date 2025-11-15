package org.dochi.webserver.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// 등록된 shutdown hook 순차적으로 실행 보장
public class ShutdownTasksProcessor {
    private static final Logger log = LoggerFactory.getLogger(ShutdownTasksProcessor.class);
    private final List<Runnable> tasks = new CopyOnWriteArrayList<>();

    public void add(Runnable task) { tasks.add(task); }
    public void remove(Runnable task) { tasks.remove(task); }
    public int count() { return tasks.size(); }

    public void run() {
        log.info("Shutdown started: {} tasks", tasks.size());
        for (Runnable t : tasks) {
            try {
                t.run();
            } catch (Exception e) {
                log.error("Shutdown task failed", e);
            }
        }
        log.info("Shutdown complete.");
    }
}