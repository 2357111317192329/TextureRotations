import texture.TextureProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TextureFinder extends Thread {

    public static final ArrayList<RotationInfo> formation = new ArrayList<>();
    private static final List<RotationInfo> topsAndBottoms = new ArrayList<>();
    private static final List<RotationInfo> sides = new ArrayList<>();
    private List<int[]> points;
    private int d;
    public static final int[] dx;
    public static final int[] dy;
    public static final int[] dz;
    public static final int[] expected;
    public static final int N;
    private final AtomicInteger nextD;
    private final int maxRadius;
    private final GpuRunner gpuRunner;
    private boolean hadlog=false;
    private long startTime = System.currentTimeMillis();

    //search parameters are in the Main class

    static {
        // data y62 大概率為1.21.1
        formation.add(new RotationInfo(0, 0, 1, 3, false));
        formation.add(new RotationInfo(0, 0, 2, 3, false));
        formation.add(new RotationInfo(0, 0, 3, 0, false));
        formation.add(new RotationInfo(1, 0, 1, 3, false));
        formation.add(new RotationInfo(1, 0, 0, 3, false));
        formation.add(new RotationInfo(3, 0, 0, 3, false));
        formation.add(new RotationInfo(3, 0, 1, 0, false));
        formation.add(new RotationInfo(4, 0, 0, 1, false));
        formation.add(new RotationInfo(4, 0, 1, 3, false));
        formation.add(new RotationInfo(0, 0, -1, 2, false));
        formation.add(new RotationInfo(-1, 0, 0, 0, false));
        formation.add(new RotationInfo(-1, 0, 1, 3, false));
        formation.add(new RotationInfo(-4, 0, 0, 1, false));
        formation.add(new RotationInfo(-4, 0, -1, 0, false));
        formation.add(new RotationInfo(-5, 0, 0, 3, false));
        formation.add(new RotationInfo(-5, 0, -1, 0, false));
        formation.add(new RotationInfo(-8, 0, 1, 3, false));
        formation.add(new RotationInfo(-8, 0, 3, 3, false));
        formation.add(new RotationInfo(-9, 0, 3, 0, false));
        formation.add(new RotationInfo(-11, 0, 1, 1, false));
        formation.add(new RotationInfo(-11, 0, 0, 3, false));
        formation.add(new RotationInfo(-11, 0, -1, 0, false));
        formation.add(new RotationInfo(-12, 0, -1, 2, false));
        //
        //for 1.21.8 testing y63
        //
        //formation.add(new RotationInfo(1, 0, 0, 3, false));
        //formation.add(new RotationInfo(2, 0, 0, 0, false));
        //formation.add(new RotationInfo(0, 0, 1, 1, false));
        //formation.add(new RotationInfo(0, 0, 2, 3, false));
        //formation.add(new RotationInfo(1, 0, 1, 2, false));
        //formation.add(new RotationInfo(2, 0, 1, 0, false));
        //formation.add(new RotationInfo(1, 0, 2, 3, false));
        //formation.add(new RotationInfo(2, 0, 2, 3, false));
        //formation.add(new RotationInfo(-1, 0, 0, 1, false));
        //formation.add(new RotationInfo(-2, 0, 0, 3, false));
        //formation.add(new RotationInfo(-1, 0, 1, 2, false));
        //formation.add(new RotationInfo(-1, 0, 2, 2, false));
        //formation.add(new RotationInfo(-2, 0, 1, 2, false));
        //formation.add(new RotationInfo(-2, 0, 2, 1, false));
        //formation.add(new RotationInfo(0, 0, -1, 1, false));
        //formation.add(new RotationInfo(0, 0, -2, 2, false));
        //formation.add(new RotationInfo(1, 0, -1, 1, false));
        //formation.add(new RotationInfo(2, 0, -1, 1, false));
        //formation.add(new RotationInfo(1, 0, -2, 1, false));
        //formation.add(new RotationInfo(2, 0, -2, 1, false));
        //formation.add(new RotationInfo(-1, 0, -1, 3, false));
        //formation.add(new RotationInfo(-2, 0, -1, 1, false));
        //formation.add(new RotationInfo(-1, 0, -2, 2, false));
        //formation.add(new RotationInfo(-2, 0, -2, 1, false));
        //
        //for 1.21.1 testing (x,y,z)=(2020749,90,8814886) 
        //
        //formation.add(new RotationInfo(1, 0, 0, 1, false));
        //formation.add(new RotationInfo(2, 0, 0, 2, false));
        //formation.add(new RotationInfo(0, 0, 1, 0, false));
        //formation.add(new RotationInfo(0, 0, 2, 1, false));
        //formation.add(new RotationInfo(1, 0, 1, 0, false));
        //formation.add(new RotationInfo(2, 0, 1, 3, false));
        //formation.add(new RotationInfo(1, 0, 2, 2, false));
        //formation.add(new RotationInfo(2, 0, 2, 3, false));
        //formation.add(new RotationInfo(-1, 0, 0, 2, false));
        //formation.add(new RotationInfo(-2, 0, 0, 1, false));
        //formation.add(new RotationInfo(-1, 0, 1, 1, false));
        //formation.add(new RotationInfo(-1, 0, 2, 3, false));
        //formation.add(new RotationInfo(-2, 0, 1, 2, false));
        //formation.add(new RotationInfo(-2, 0, 2, 1, false));
        //formation.add(new RotationInfo(0, 0, -1, 2, false));
        //formation.add(new RotationInfo(0, 0, -2, 0, false));
        //formation.add(new RotationInfo(1, 0, -1, 0, false));
        //formation.add(new RotationInfo(2, 0, -1, 0, false));
        //formation.add(new RotationInfo(1, 0, -2, 3, false));
        //formation.add(new RotationInfo(2, 0, -2, 2, false));
        //formation.add(new RotationInfo(-1, 0, -1, 1, false));
        //formation.add(new RotationInfo(-2, 0, -1, 1, false));
        //formation.add(new RotationInfo(-1, 0, -2, 3, false));
        //formation.add(new RotationInfo(-2, 0, -2, 0, false));
        //
        //for ch3 testing y90-100 判定為1.21.1
        //
        //formation.add(new RotationInfo(0, 0, 3, 2, false));
        //formation.add(new RotationInfo(1, 1, 1, 3, false));
        //formation.add(new RotationInfo(0, 2, 1, 1, false));
        //formation.add(new RotationInfo(-1, 3, 1, 2, false));
        //formation.add(new RotationInfo(-1, 3, 0, 3, false));
        //formation.add(new RotationInfo(-2, 4, 0, 0, false));
        //formation.add(new RotationInfo(-3, 4, 0, 0, false));
        //formation.add(new RotationInfo(-3, 4, 1, 0, false));
        //formation.add(new RotationInfo(-3, 4, 2, 3, false));
        //formation.add(new RotationInfo(-5, 5, 2, 1, false));
        //formation.add(new RotationInfo(-6, 6, 2, 3, false));
        //formation.add(new RotationInfo(-8, 6, 4, 3, false));
        //formation.add(new RotationInfo(-8, 5, 5, 2, false));
        //formation.add(new RotationInfo(-1, 1, 0, 2, false));
        //formation.add(new RotationInfo(-2, 1, 1, 3, false));
        //formation.add(new RotationInfo(-2, 1, 2, 1, false));
        //formation.add(new RotationInfo(-2, 1, 5, 1, false));
        //formation.add(new RotationInfo(-3, 1, 5, 0, false));
        //formation.add(new RotationInfo(-3, 1, 6, 3, false));
        //formation.add(new RotationInfo(-4, 1, 6, 0, false));
        //formation.add(new RotationInfo(-4, 1, 7, 2, false));
        //formation.add(new RotationInfo(-2, 0, 7, 3, false));
        //formation.add(new RotationInfo(-3, 0, 9, 0, false));
        //formation.add(new RotationInfo(-5, 0, 11, 3, false));
        //formation.add(new RotationInfo(-6, 0, 11, 3, false));
        N = formation.size();
        dx = new int[N];
        dy = new int[N];
        dz = new int[N];
        expected = new int[N];
        for (int i = 0; i < formation.size(); i++) {
            RotationInfo b = formation.get(i);
            dx[i] = b.x;
            dy[i] = b.y;
            dz[i] = b.z;
            expected[i] = b.rotation;
        }
    }
    private final TextureProvider textureProvider;
    private final ProgressTracker progress;
    TextureFinder(AtomicInteger nextD, int maxRadius, TextureProvider mode,GpuRunner gpuRunner,ProgressTracker progress) {
        this.nextD = nextD;
        this.maxRadius = maxRadius;
        this.textureProvider = mode;
        this.gpuRunner = gpuRunner;
        this.progress = progress;
    }

    public void run() {
        int yMin=Main.yMin;
        int yMax=Main.yMin;
        while (true) {
            while (PauseController.paused) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (Main.STOP) break;
            int d = nextD.getAndIncrement();
            //System.out.println(gpuRunner.getid() + " got d=" + d);
            if (hadlog){
                startTime = System.currentTimeMillis();
                hadlog=false;
            }
            if (d > maxRadius) break;
            for(int y=yMin;y<=yMax;y++){
                List<Result> results;
                try {
                    results = gpuRunner.run(d,y);
                } catch (Exception e) {
                    Main.STOP = true;
                    e.printStackTrace();
                    break;
                }
                if(results != null && !results.isEmpty()){
                    for (Result r : results) {
                        int x = r.x;
                        int z = r.z;
                        boolean ok = true;
                        System.out.println("X: " + x + " Y: " + y + " Z: " + z+" d="+d);
                        for (int i = 0; i < N; i++) {
                            int tx = r.x + dx[i];
                            int ty = y + dy[i];
                            int tz = r.z + dz[i];
                            int val = textureProvider.getTexture(tx, ty, tz, 4);
                            if (val != expected[i]) {
                                ok = false;

                                System.out.println("Mismatch!");
                                System.out.println("fail at x=" + r.x +" y="+y+" z=" + r.z+" d="+d+" i=" + i+", expected="+expected[i]+", CPU get="+val);
                                break;
                            }
                        }
                        if (!ok) {
                            throw new RuntimeException("GPU incorrect result detected");
                        }
                    }
                    PauseController.paused = true;
                    System.out.println("=== PAUSED: press Enter to continue ===");
                    synchronized (Main.class) {
                        try (FileWriter fw = new FileWriter("results.txt", true);
                            BufferedWriter bw = new BufferedWriter(fw)) {
                            for (Result r : results) {
                                bw.write("X: " + r.x + " Y: " + y + " Z: " + r.z + " d=" + d);
                                bw.newLine();
                            }
                            bw.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            progress.markDone(d);
            if(d%41==1){
                if((System.currentTimeMillis() - startTime)>1000){
                    System.out.println("contiguous completed d = " + progress.contiguousCompletedD());
                    hadlog=true;
                }
            }
            
            while (PauseController.paused) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } 
    }
}
