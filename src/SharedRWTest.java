import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SharedRWTest {

    private enum Status {
        IDLE,
        READ,
        WRITE
    }

    private int readers = 0;
    private Status currentStatus = Status.IDLE;

    public static void main(String args[]) {

        SharedRW sharedRW = new SharedRWNotifyWait();
        SharedRWTest sharedRWTest = new SharedRWTest();

         class Reader implements Callable<Void> {

            @Override
            public Void call() {
                try {
                    sharedRW.acquireReadLock();
                } catch (Exception e) {
                    e.getMessage();
                    return null;
                }

                synchronized (sharedRWTest) {
                    assert sharedRWTest.currentStatus != Status.WRITE : "Performing READ while ongoing WRITE";
                    sharedRWTest.readers++;
                    sharedRWTest.currentStatus = Status.READ;
                }


                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                } finally {
                    synchronized (sharedRWTest) {
                        if (--sharedRWTest.readers == 0) {
                            sharedRWTest.currentStatus = Status.IDLE;
                        }
                    }
                    sharedRW.releaseReadLock();
                }

                return null;
            }
        }

         class Writer implements Callable<Void> {
            @Override
            public Void call() {
                try {
                    sharedRW.acquireWriteLock();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return null;
                }

                synchronized (sharedRWTest) {
                    assert sharedRWTest.currentStatus == Status.IDLE : "Performing WRITE while ongoing READ or WRITE";
                    sharedRWTest.currentStatus = Status.WRITE;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                } finally {
                    synchronized (sharedRWTest) {
                        sharedRWTest.currentStatus = Status.IDLE;
                    }
                    sharedRW.releaseWriteLock();
                }

                return null;
            }
        }

        Callable<Void> reader = new Reader();
        Callable<Void> writer = new Writer();

        boolean success = true;
        System.out.println("Starting test!");

        int numThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> future =  new ArrayList<>(1000);
        for (int i = 0; i < numThreads; i++) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                future.add(executor.submit(reader));
            } else {
                future.add(executor.submit(writer));
            }
        }

        executor.shutdown();

        for (int i = 0; i < numThreads; i++) {
            try {
                future.get(i).get();
            } catch (CancellationException | InterruptedException e) {
                System.out.println(e.getMessage());
            } catch (ExecutionException e) {
                System.out.print("Error: " + e.getMessage());
            }
        }

        try {
            executor.awaitTermination(5, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            success = false;
            System.out.println("Error: " + e.getMessage());
        }

        if (success) {
            System.out.println("Test succeeded!");
        } else {
            System.out.println("Test failed!");
        }
    }

}
