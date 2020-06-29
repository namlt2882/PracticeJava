package org.learning;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parallel {
    private static final int MAX_THREAD = 3;

    private static List<Callable<Integer>> callables;

    static {
        callables = IntStream.rangeClosed(0, 10).boxed().map(index -> (Callable<Integer>) () -> {
            Thread.sleep(2000);
            System.out.print(index + ", ");
            return index;
        }).collect(Collectors.toList());
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("-- Executor Service");
        runWithExecutorService();

        System.out.println("\n-- Parallel Stream");
        runWithParallelStream();

        System.out.println("\n-- Parallel Stream limit threads by ForkJoinPool");
        runParallelStreamInsidePool();

        System.out.println("\n-- CompletableFuture with allOf()");
        runWithCompletableFutureWithAllOf();

        System.out.println("\n-- CompletableFuture with allOf() and limit timeout 3s");
        runWithCompletableFutureWithTimeout(3);

        System.out.println("\n-- CompletableFuture");
        runWithCompletableFuture();

        System.out.println("\n-- CompletableFuture limits threads by Executor Service");
        runExecutorWithCompletableFuture();

    }

    public static void runParallelStreamInsidePool() throws ExecutionException, InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(MAX_THREAD);
        List<Integer> finalRs = pool.submit(() -> callables.parallelStream()
                .map(callable -> {
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()))
                .join();
        pool.shutdown();
        System.out.println("\nFinal result: " + finalRs.toString());
    }

    public static void runExecutorWithCompletableFuture() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
        List<Integer> finalRs = new ArrayList<>();
        List<Future<Integer>> futures = callables.stream().map(callable -> {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, pool);
        }).collect(Collectors.toList());
        futures.forEach(future -> {
            try {
                finalRs.add(future.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        pool.shutdown();
        System.out.println("\nFinal result: " + finalRs.toString());
    }

    public static void runWithCompletableFutureWithTimeout(int timeout) throws ExecutionException, InterruptedException {
        List<Integer> finalRs = new ArrayList<>();
        AtomicBoolean canceled = new AtomicBoolean(false);
        List<CompletableFuture<Void>> futures = callables.stream().map(callable -> {
            return CompletableFuture.runAsync(() -> {
                try {
                    Integer rs = callable.call();
                    if (!canceled.get()) {
                        finalRs.add(rs);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }).collect(Collectors.toList());
        CompletableFuture<Void> completeFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[futures.size()])
        );
        try {
            completeFuture.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            canceled.set(true);
        }
        System.out.println("\nStop execution! Waiting 2s to finished!");
        Thread.sleep(2000);
        System.out.println("\nFinal result: " + finalRs.toString());
    }

    public static void runWithCompletableFutureWithAllOf() throws ExecutionException, InterruptedException {
        List<CompletableFuture<Integer>> futures = callables.stream().map(callable -> {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }).collect(Collectors.toList());
        CompletableFuture<List<Integer>> completeFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[futures.size()])
        ).thenApply((param) -> futures.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()));
        List<Integer> finalRs = completeFuture.get();
        System.out.println("\nFinal result: " + finalRs.toString());
    }

    public static void runWithCompletableFuture() throws ExecutionException, InterruptedException {
        List<Integer> finalRs = new ArrayList<>();
        callables.stream().forEach(callable -> {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(integer -> {
                finalRs.add(integer);
            });
        });

        System.out.println("Submitted. Wait 8000ms to complete all!");
        Thread.sleep(8000);
        System.out.println("\nFinal result: " + finalRs.toString());
    }

    public static void runWithParallelStream() {
        List<Integer> finalRs = callables.parallelStream().map(callable -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        System.out.println("\nFinal result: " + finalRs.toString());
    }

    public static void runWithExecutorService() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD);
        List<Future<Integer>> results;
        results = executorService.invokeAll(callables);
        List<Integer> finalRs = results.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        executorService.shutdown();
        System.out.println("\nFinal result: " + finalRs.toString());
    }
}
