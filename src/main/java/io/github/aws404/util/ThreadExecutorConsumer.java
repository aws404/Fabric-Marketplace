package io.github.aws404.util;

import com.google.common.collect.Queues;
import net.minecraft.util.thread.MessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class ThreadExecutorConsumer<R extends Consumer<T>, T extends ThreadExecutorConsumer<R, T>> implements MessageListener<R>, Executor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String name;
    private final Queue<R> tasks = Queues.newConcurrentLinkedQueue();
    protected int executionsInProgress;

    protected ThreadExecutorConsumer(String name) {
        this.name = name;
    }

    protected abstract R createTask(Runnable runnable);

    protected abstract boolean canExecute(R task);

    public boolean isOnThread() {
        return Thread.currentThread() == this.getThread();
    }

    protected abstract Thread getThread();

    protected boolean shouldExecuteAsync() {
        return !this.isOnThread();
    }

    public int getTaskCount() {
        return this.tasks.size();
    }

    public String getName() {
        return this.name;
    }

    public void send(R runnable) {
        this.tasks.add(runnable);
        LockSupport.unpark(this.getThread());
    }

    public void execute(Runnable runnable) {
        if (this.shouldExecuteAsync()) {
            this.send(this.createTask(runnable));
        } else {
            runnable.run();
        }

    }

    protected void cancelTasks() {
        this.tasks.clear();
    }

    protected boolean runTask() {
        R runnable = this.tasks.peek();
        if (runnable == null) {
            return false;
        } else if (this.executionsInProgress == 0 && !this.canExecute(runnable)) {
            return false;
        } else {
            this.executeTask(this.tasks.remove());
            return true;
        }
    }

    public void runTasks(BooleanSupplier stopCondition) {
        ++this.executionsInProgress;

        try {
            while (!stopCondition.getAsBoolean()) {
                if (!this.runTask()) {
                    this.waitForTasks();
                }
            }
        } finally {
            --this.executionsInProgress;
        }

    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void executeTask(R task) {
        try {
            task.accept((T) this);
        } catch (Exception var3) {
            LOGGER.fatal("Error executing task on {}", this.getName(), var3);
        }

    }
}