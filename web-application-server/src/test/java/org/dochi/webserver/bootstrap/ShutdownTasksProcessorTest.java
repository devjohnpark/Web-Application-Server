package org.dochi.webserver.bootstrap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ShutdownTasksProcessorTest {

    private ShutdownTasksProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ShutdownTasksProcessor();
    }

    @Test
    void run_executesRegisteredTask() {
        AtomicBoolean executed = new AtomicBoolean(false);
        processor.add(() -> executed.set(true));

        processor.run();

        assertThat(executed.get()).isTrue();
    }

    @Test
    void run_executesInOrder() {
        List<Integer> order = new CopyOnWriteArrayList<>();
        processor.add(() -> order.add(1));
        processor.add(() -> order.add(2));
        processor.add(() -> order.add(3));

        processor.run();

        assertThat(order).containsExactly(1, 2, 3);
    }

    @Test
    void remove_removedTaskNotExecuted() {
        AtomicBoolean executed = new AtomicBoolean(false);
        Runnable task = () -> executed.set(true);

        processor.add(task);
        processor.remove(task);
        processor.run();

        assertThat(executed.get()).isFalse();
    }

    @Test
    void count_returnsCorrectCount() {
        processor.add(() -> {});
        processor.add(() -> {});

        assertThat(processor.count()).isEqualTo(2);
    }
}