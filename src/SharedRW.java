public abstract class SharedRW {
    public abstract void acquireReadLock() throws InterruptedException;
    public abstract void releaseReadLock();
    public abstract void acquireWriteLock() throws InterruptedException;
    public abstract void releaseWriteLock();
}
