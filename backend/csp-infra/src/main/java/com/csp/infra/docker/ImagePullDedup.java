package com.csp.infra.docker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 镜像拉取去重：同一镜像并发拉取时共享同一个 Future，避免重复下载。
 * <p>对齐 GZCTF 的 ImagePull 机制。</p>
 */
@Slf4j
public class ImagePullDedup {

    private final ConcurrentMap<String, PullTask> pulls = new ConcurrentHashMap<>();

    public CompletableFuture<Boolean> acquire(String image, java.util.function.Function<String, CompletableFuture<Boolean>> puller) {
        while (true) {
            PullTask task = pulls.computeIfAbsent(image, k -> new PullTask(puller.apply(k)));
            if (task.tryAddWaiter()) {
                return task.future;
            }
            pulls.remove(image, task);
        }
    }

    public void complete(String image, boolean success) {
        PullTask task = pulls.remove(image);
        if (task != null) {
            task.future.complete(success);
            task.cancelToken.cancel(false);
        }
    }

    public void fail(String image, Throwable ex) {
        PullTask task = pulls.remove(image);
        if (task != null) {
            task.future.completeExceptionally(ex);
            task.cancelToken.cancel(false);
        }
    }

    private static class PullTask {
        final CompletableFuture<Boolean> future;
        final CancellationToken cancelToken;
        int waiters;
        boolean closed;

        PullTask(CompletableFuture<Boolean> future) {
            this.future = future;
            this.cancelToken = new CancellationToken();
        }

        synchronized boolean tryAddWaiter() {
            if (closed) return false;
            waiters++;
            return true;
        }

        synchronized void removeWaiter() {
            if (--waiters <= 0) {
                closed = true;
                cancelToken.cancel(false);
            }
        }
    }

    public static class CancellationToken {
        private volatile boolean cancelled;

        public void cancel(boolean force) {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}
