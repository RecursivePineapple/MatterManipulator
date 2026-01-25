package matter_manipulator.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.GlobalMMConfig;
import matter_manipulator.MMMod;
import matter_manipulator.common.utils.MCUtils;

/// A simple coroutine scheduler that will run expensive tasks in the background without multithreading.
/// This coroutine scheduler differs from typical schedulers in that it will try to run the oldest tasks first, without
/// considering newer tasks.
/// This is to prevent it from becoming clogged if something's generating bad tasks.
public final class CoroutineExecutor {

    public static final CoroutineExecutor CLIENT = new CoroutineExecutor(Side.CLIENT);
    public static final CoroutineExecutor SERVER = new CoroutineExecutor(Side.SERVER);

    private final Side side;

    private final LinkedHashMap<String, CoroutineFutureImpl<?>> tasks = new LinkedHashMap<>();
    private final List<CoroutineFutureImpl<?>> newTasks = new ArrayList<>();

    private long start, end;

    private CoroutineExecutor(Side side) {
        this.side = side;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != Phase.END && event.side != this.side) return;

        if (tasks.isEmpty() && newTasks.isEmpty()) return;

        start = System.nanoTime();
        end = 0;

        // we intentionally don't return to the previously ran task here
        Iterator<CoroutineFutureImpl<?>> iter = tasks.values()
            .iterator();

        while (iter.hasNext()) {
            CoroutineFutureImpl<?> future = iter.next();

            long start2 = 0;

            if (GlobalMMConfig.DebugConfig.schedulerProfileLevel >= 2) {
                start2 = System.nanoTime();
            }

            if (!future.running) {
                iter.remove();
                continue;
            }

            try {
                future.run();
            } catch (Throwable t) {
                MMMod.LOG.error(
                    "Caught error while running task; it will be cancelled ({} {})",
                    future.name,
                    future.task,
                    t);
                future.abort(t);
            }

            if (!future.running) {
                iter.remove();
            }

            // poll the time if the task hasn't done it already via shouldYield
            if (end == 0) end = System.nanoTime();

            if (GlobalMMConfig.DebugConfig.schedulerProfileLevel >= 2) {
                MMMod.LOG.info(
                    "Task {} {} took {} microseconds",
                    future.name,
                    future.task,
                    MCUtils.formatNumbers((end - start2) / 1e3));
            }

            if (end - start > GlobalMMConfig.DebugConfig.schedulerDuration) {
                break;
            }
        }

        if (GlobalMMConfig.DebugConfig.schedulerProfileLevel >= 1) {
            MMMod.LOG.info(
                "Task scheduler took {} microseconds",
                MCUtils.formatNumbers((System.nanoTime() - start) / 1e3));
        }

        for (CoroutineFutureImpl<?> future : newTasks) {
            future = tasks.put(future.name, future);

            if (future != null) {
                future.cancelled = true;
                future.running = false;
            }
        }

        newTasks.clear();
    }

    public <T> CoroutineFuture<T> schedule(Coroutine<T> task) {
        CoroutineFutureImpl<T> future = new CoroutineFutureImpl<>(UUID.randomUUID().toString(), task);

        newTasks.add(future);

        return future;
    }

    public <T> CoroutineFuture<T> schedule(String name, Coroutine<T> task) {
        CoroutineFutureImpl<T> future = new CoroutineFutureImpl<>(name, task);

        newTasks.add(future);

        return future;
    }

    private class CoroutineFutureImpl<T> implements CoroutineFuture<T>, CoroutineExecutionContext<T> {
        private final String name;
        private final Coroutine<T> task;

        private boolean running = true, cancelled = false;
        private T value;
        private Throwable error;
        private Consumer<T> callback;

        public CoroutineFutureImpl(String name, Coroutine<T> task) {
            this.name = name;
            this.task = task;
        }

        private void abort(Throwable error) {
            if (running) {
                running = false;
                cancelled = true;
                this.error = error;
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (running) {
                running = false;
                cancelled = true;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public T get() throws ExecutionException {
            if (running) {
                throw new IllegalStateException("cannot call get() if the task has not finished");
            }

            if (error != null) {
                throw new ExecutionException(error);
            }

            return value;
        }

        @Override
        public T get(long timeout, @NotNull TimeUnit unit) throws ExecutionException {
            return get();
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return !running;
        }

        private void run() {
            task.run(this);
        }

        @Override
        public boolean shouldYield() {
            return (end = System.nanoTime()) - start
                > GlobalMMConfig.DebugConfig.schedulerDuration / Math.min(tasks.size(), GlobalMMConfig.DebugConfig.maxTaskCount);
        }

        @Override
        public void stop(T value) {
            running = false;
            this.value = value;
            if (callback != null) {
                callback.accept(value);
            }
        }

        @Override
        public void setCallback(Consumer<T> callback) {
            this.callback = callback;
        }
    }
}
