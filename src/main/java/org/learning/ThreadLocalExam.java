package org.learning;

public class ThreadLocalExam {
    public static void main(String[] args) throws InterruptedException {
        Crawler crawler = new Crawler();
        Thread thread1 = new Thread(() -> {
            try {
                crawler.processingData("1");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Request 1");
        Thread thread2 = new Thread(() -> {
            try {
                crawler.processingData("2");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Request 2");
        thread1.start();
        thread2.start();
    }

    public static class Crawler {
        private final ThreadLocal<String> data = new ThreadLocal<>();

        public void processingData(String requestId) throws InterruptedException {
            System.out.println(Thread.currentThread().getName() + ": Set data for request id " + requestId);
            data.set(Thread.currentThread().getName() + ": Data for request id " + requestId);
            int i = 5;
            while (i-- > 0) {
                printDataValue();
                Thread.sleep(2000);
            }
            data.remove();
        }

        private void printDataValue() {
            System.out.println(data.get());
        }
    }
}
