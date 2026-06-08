import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PauseController {
    public static volatile boolean paused = false;
}