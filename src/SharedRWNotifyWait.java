
public class SharedRWNotifyWait extends SharedRW {

    private final static int maxReaders = 100;
    private int activeReaders = 0;
    private boolean activeWriter = false;
    private final Object readLock = new Object();
    private final Object writeLock = new Object();

    @Override
    public void acquireReadLock() throws InterruptedException {
        synchronized (readLock) {
            while (activeReaders == maxReaders) {
                readLock.wait();
            }
            activeReaders++;
        }
    }

    @Override
    public void releaseReadLock() {
        synchronized (readLock) {
            activeReaders--;
            readLock.notify();
        }
    }

    @Override
    public void acquireWriteLock() throws InterruptedException {
        synchronized (writeLock) {
            while (activeWriter) {
                writeLock.wait();
            }
            activeWriter = true;
            synchronized (readLock) {
                int actualReaders = activeReaders;
                activeReaders = maxReaders;
                while (actualReaders > 0) {
                    while (activeReaders == maxReaders) {
                        readLock.wait();
                    }
                    actualReaders -= maxReaders - activeReaders;
                    activeReaders = maxReaders;
                }
            }
        }
    }

    @Override
    public void releaseWriteLock() {
        synchronized (writeLock) {
            synchronized (readLock) {
                activeReaders = 0;
                readLock.notifyAll();
            }
            activeWriter = false;
            writeLock.notify();
        }
    }

}

