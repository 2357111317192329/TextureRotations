import org.jocl.*;
import static org.jocl.CL.*;
import texture.TextureProvider;
import texture.VanillaTextures;
import texture.SodiumTextures;
import texture.Sodium19Textures;
import texture.Vanilla12Textures;
import texture.Vanilla21_1Textures;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static final int xMin = -29999984, xMax = 29999984;
    public static final int zMin = -29999984, zMax = 29999984;
    public static final int yMin = 62    , yMax = 62;//yMin = 62    , yMax = 62;
    public static final int threads = 4;
    public static final int BATCH_SIZE=10000000;
    public static boolean STOP = false;
    public static final Scanner SC = new Scanner(System.in);
    public static final TextureProvider mode = new Vanilla21_1Textures();
    //                                         new VanillaTextures();
    //                                         new SodiumTextures();
    //                                         new Sodium19Textures();
    //                                         new Vanilla12Textures();
    //                                         new Vanilla21_1Textures();

    //goto TextureFinder class to configure rotations
    public static void main(String[] args) throws InterruptedException {
        Thread listener = new Thread(() -> {
            try {
                while (true) {
                    int c = System.in.read(); // 讀任意鍵
                    if (c != -1) {
                        PauseController.paused = false;
                        System.out.println("=== RESUMED ===");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
        int maxRadius = 29999984;//29999984
        List<cl_device_id> gpuDevices = new ArrayList<>();
        int[] numPlatformsArr = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArr);
        int numPlatforms = numPlatformsArr[0];
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(numPlatforms, platforms, null);
        for (int i = 0; i < numPlatforms; i++) {
            int[] numDevicesArr = new int[1];
            int err = clGetDeviceIDs(platforms[i],
                    CL_DEVICE_TYPE_GPU,
                    0, null, numDevicesArr);
            if (err != CL_SUCCESS || numDevicesArr[0] == 0) continue;
            int numDevices = numDevicesArr[0];
            cl_device_id[] devices = new cl_device_id[numDevices];

            clGetDeviceIDs(platforms[i],
                    CL_DEVICE_TYPE_GPU,
                    numDevices, devices, null);

            for (cl_device_id d : devices) {
                gpuDevices.add(d);
            }
        }
        System.out.println("Total GPU found: " + gpuDevices.size());
        List<GpuRunner> runners = new ArrayList<>();
        for (cl_device_id dev : gpuDevices) {
            runners.add(new GpuRunner(dev));
        }
        int startd=23018178;//10641139
        AtomicInteger nextD = new AtomicInteger(startd);
        ProgressTracker progress = new ProgressTracker(startd);
        List<TextureFinder> workers = new ArrayList<>();
        for (GpuRunner runner : runners) {
            TextureFinder worker = new TextureFinder(nextD, maxRadius, mode, runner,progress);
            workers.add(worker);
            worker.start();
        }
        for (TextureFinder w : workers) {
            w.join();
        }
    }
}
