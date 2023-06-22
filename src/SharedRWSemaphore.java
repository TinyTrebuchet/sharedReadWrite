import java.util.concurrent.*;

public class SharedRWSemaphore extends SharedRW {

    private final int maxReaders = 100;
    private final Semaphore readLock = new Semaphore(maxReaders);
    private final Semaphore writeLock = new Semaphore(1);

    @Override
    public void acquireReadLock() throws InterruptedException {
        readLock.acquire();
    }

    @Override
    public void releaseReadLock() {
        readLock.release();
    }

    @Override
    public void acquireWriteLock() throws InterruptedException {
        writeLock.acquire();
        readLock.acquire(maxReaders);
    }

    @Override
    public void releaseWriteLock() {
        readLock.release(maxReaders);
        writeLock.release();
    }

}

