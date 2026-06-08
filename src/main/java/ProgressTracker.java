import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressTracker {
    private final AtomicInteger nextExpected;
    private final Set<Integer> done = ConcurrentHashMap.newKeySet();

    public ProgressTracker(int startD) {
        this.nextExpected = new AtomicInteger(startD);
    }

    public void markDone(int d) {
        done.add(d);
        synchronized (this) {
            while (done.remove(nextExpected.get())) {
                nextExpected.incrementAndGet();
            }
        }
    }

    public int contiguousCompletedD() {
        return nextExpected.get() - 1;
    }
}