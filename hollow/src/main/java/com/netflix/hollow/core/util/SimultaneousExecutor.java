/*
 *  Copyright 2016-2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.core.util;

import org.checkerframework.dataflow.qual.Impure;
import static com.netflix.hollow.core.util.Threads.daemonThread;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A convenience wrapper around ThreadPoolExecutor. Provides sane defaults to
 * constructor arguments and allows for awaitUninterruptibly().
 * 
 * <p><strong>Internal Use:</strong> This class is intended for internal 
 * framework use and is not meant for external consumption.
 */
public class SimultaneousExecutor extends ThreadPoolExecutor {

    private static final String DEFAULT_THREAD_NAME = "simultaneous-executor";

    private final ConcurrentLinkedQueue<Future<?>> futures = new ConcurrentLinkedQueue<>();

    /**
     * Creates an executor with a thread per processor.
     * <p>
     * Equivalent to constructing a {@code SimultaneousExecutor} with {@code 1.0d}
     * threads per CPU.
     */
    @Impure
    public SimultaneousExecutor(Class<?> context, String description) {
        this(1.0d, context, description);
    }

    /**
     * Creates an executor with a thread per processor.
     * <p>
     * Equivalent to calling {@code SimultaneousExecutor(1.0d)}
     *
     * @deprecated use {@link #SimultaneousExecutor(Class, String)}
     */
    @Impure
    @Deprecated
    public SimultaneousExecutor() {
        this(1.0d, SimultaneousExecutor.class, DEFAULT_THREAD_NAME);
    }

    /**
     * Creates an executor with number of threads calculated from the
     * specified factor.
     *
     * @param threadsPerCpu calculated as {@code processors * threadsPerCpu} then used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param context used to name created threads
     */
    @Impure
    public SimultaneousExecutor(double threadsPerCpu, Class<?> context) {
        this(threadsPerCpu, context, DEFAULT_THREAD_NAME);
    }

    /**
     * Creates an executor with number of threads calculated from the
     * specified factor.
     *
     * @param threadsPerCpu calculated as {@code processors * threadsPerCpu} then used as {@code corePoolSize} and {@code maximumPoolSize}
     *
     * @deprecated use {@link #SimultaneousExecutor(double, Class)}
     */
    @Impure
    @Deprecated
    public SimultaneousExecutor(double threadsPerCpu) {
        this(threadsPerCpu, SimultaneousExecutor.class, DEFAULT_THREAD_NAME);
    }

    /**
     * Creates an executor with number of threads calculated from the
     * specified factor and threads named according to {@code context} and {@code description}.
     *
     * @param threadsPerCpu calculated as {@code processors * threadsPerCpu} then used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param context combined with {@code description} to name created threads
     * @param description brief description used to name created threads; combined with {@code context}
     */
    @Impure
    public SimultaneousExecutor(double threadsPerCpu, Class<?> context, String description) {
        this((int) ((double) Runtime.getRuntime().availableProcessors() * threadsPerCpu), context, description);
    }

    /**
     * Creates an executor with number of threads calculated from the
     * specified factor and threads named according to {@code description}.
     *
     * @param threadsPerCpu calculated as {@code processors * threadsPerCpu} then used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param description brief description used to name created threads
     *
     * @deprecated use {@link #SimultaneousExecutor(double, Class, String)}
     */
    @Impure
    @Deprecated
    public SimultaneousExecutor(double threadsPerCpu, String description) {
        this((int) ((double) Runtime.getRuntime().availableProcessors() * threadsPerCpu), SimultaneousExecutor.class, description);
    }

    /**
     * Creates an executor with the specified number of threads and threads named
     * according to {@code context}.
     *
     * @param numThreads used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param context used to name created threads
     */
    @Impure
    public SimultaneousExecutor(int numThreads, Class<?> context) {
        this(numThreads, context, DEFAULT_THREAD_NAME);
    }

    /**
     * Creates an executor with the specified number of threads.
     *
     * @param numThreads used as {@code corePoolSize} and {@code maximumPoolSize}
     *
     * @deprecated use {@link #SimultaneousExecutor(int, Class)}
     */
    @Impure
    @Deprecated
    public SimultaneousExecutor(int numThreads) {
        this(numThreads, SimultaneousExecutor.class, DEFAULT_THREAD_NAME);
    }

    /**
     * Creates an executor with the specified number of threads and threads named
     * according to {@code context} and {@code description}.
     *
     * @param numThreads used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param context combined with {@code description} to name created threads
     * @param description brief description used to name created threads; combined with {@code context}
     */
    @Impure
    public SimultaneousExecutor(int numThreads, Class<?> context, String description) {
        this(numThreads, context, description, Thread.NORM_PRIORITY);
    }


    /**
     * Creates an executor with number of threads calculated from the
     * specified factor and threads named according to {@code context} and {@code description}.
     *
     * @param threadsPerCpu calculated as {@code processors * threadsPerCpu} then used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param context combined with {@code description} to name created threads
     * @param description brief description used to name created threads; combined with {@code context}
     * @param threadPriority the priority set to each thread
     */
    @Impure
    public SimultaneousExecutor(double threadsPerCpu, Class<?> context, String description, int threadPriority) {
        this((int) ((double) Runtime.getRuntime().availableProcessors() * threadsPerCpu), context, description, threadPriority);
    }

    /**
     * Creates an executor with the specified number of threads and threads named
     * according to {@code context} and {@code description}.
     *
     * @param numThreads used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param context combined with {@code description} to name created threads
     * @param description brief description used to name created threads; combined with {@code context}
     * @param threadPriority the priority set to each thread
     */
    @Impure
    public SimultaneousExecutor(int numThreads, Class<?> context, String description, int threadPriority) {
        this(numThreads, r -> daemonThread(r, context, description, threadPriority));
    }

    @Impure
    protected SimultaneousExecutor(int numThreads, ThreadFactory threadFactory) {
        super(numThreads, numThreads, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory);
    }

    /**
     * Creates an executor with the specified number of threads and threads named
     * according to {@code description}.
     *
     * @param numThreads used as {@code corePoolSize} and {@code maximumPoolSize}
     * @param description brief description used to name created threads
     *
     * @deprecated use {@link #SimultaneousExecutor(int, Class, String)}
     */
    @Impure
    @Deprecated
    public SimultaneousExecutor(int numThreads, final String description) {
        this(numThreads, SimultaneousExecutor.class, description);
    }

    @Impure
    @Override
    public void execute(Runnable command) {
        if(command instanceof RunnableFuture) {
            super.execute(command);
        } else {
            super.execute(newTaskFor(command, Boolean.TRUE));
        }
    }

    /**
     * Awaits completion of all submitted tasks.
     *
     * After this call completes, the thread pool will be shut down.
     */
    @Impure
    public void awaitUninterruptibly() {
        shutdown();
        while (!isTerminated()) {
            try {
                awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException e) { }
        }
    }

    @Impure
    @Override
    protected final <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
        final RunnableFuture<T> task = super.newTaskFor(runnable, value);
        futures.add(task);
        return task;
    }

    @Impure
    @Override
    protected final <T> RunnableFuture<T> newTaskFor(final Callable<T> callable) {
        final RunnableFuture<T> task = super.newTaskFor(callable);
        futures.add(task);
        return task;
    }

    /**
     * Await successful completion of all submitted tasks. Throw exception of the first failed task
     * if 1 or more tasks failed.
     *
     * After this call completes, the thread pool will be shut down.
     *
     * @throws ExecutionException if a computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
    @Impure
    public void awaitSuccessfulCompletion() throws InterruptedException, ExecutionException {
        awaitUninterruptibly();
        for (final Future<?> f : futures) {
            f.get();
        }
    }

    /**
     * Await successful completion of all previously submitted tasks.  Throw exception of the first failed task
     * if 1 or more tasks failed.
     *
     * After this call completes, the thread pool will <i>not</i> be shut down and can be reused.
     * 
     * If tasks are being submitted concurrently from other threads while this method executes, 
     * the iteration over futures is weakly consistent and may not include all concurrently submitted 
     * tasks. Ideally this method should be called after all the tasks are submitted.
     *
     * @throws ExecutionException if a computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
    @Impure
    public void awaitSuccessfulCompletionOfCurrentTasks() throws InterruptedException, ExecutionException {
        Future<?> f;
        while ((f = futures.poll()) != null) {
            f.get();
        }
    }

}
