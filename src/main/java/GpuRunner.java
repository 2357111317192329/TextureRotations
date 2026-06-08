import org.jocl.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import texture.*;

import static org.jocl.CL.*;
public class GpuRunner {
    private int MAX_OUT=6400;
    private cl_context context;
    private cl_command_queue queue;
    private cl_kernel kernel;
    private cl_mem dxMem,dyMem, dzMem, expectedMem;
    private cl_mem outXMem,outZMem, counterMem;
    private cl_device_id device;
    private cl_program program;
    private int[] counterArr;
    private final int[] clearX = new int[MAX_OUT];
    private final int[] clearZ = new int[MAX_OUT];
    //private cl_mem outIMem,outValMem;
    public GpuRunner(cl_device_id device) {
        this.device = device;
        initOpenCL();
        initBuffers();
    }
    public String getid() {
        byte[] buffer = new byte[1024];
        clGetDeviceInfo(
            device,
            CL_DEVICE_NAME,
            buffer.length,
            Pointer.to(buffer),
            null
        );
        String deviceName =
            new String(buffer).trim();
        return deviceName;
    }
    private void initOpenCL() {
        cl_context_properties props = new cl_context_properties();
        props.addProperty(CL_CONTEXT_PLATFORM, getPlatform(device));
        context = clCreateContext(
            props, 1, new cl_device_id[]{device},
            null, null, null
        );
        queue = clCreateCommandQueue(context, device, 0, null);
        // compile kernel
        program = clCreateProgramWithSource(context, 1,new String[]{loadKernel()}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        long[] logSize = new long[1];
        clGetProgramBuildInfo(
            program,
            device,
            CL_PROGRAM_BUILD_LOG,
            0,
            null,
            logSize
        );

        byte[] logData = new byte[(int)logSize[0]];

        clGetProgramBuildInfo(
            program,
            device,
            CL_PROGRAM_BUILD_LOG,
            logSize[0],
            Pointer.to(logData),
            null
        );

        System.out.println("BUILD LOG:");
        System.out.println(new String(logData));
        kernel = clCreateKernel(program, "search", null);
    }
    private cl_platform_id getPlatform(cl_device_id device) {
        cl_platform_id[] platforms = new cl_platform_id[10];
        int[] num = new int[1];
        clGetPlatformIDs(platforms.length, platforms, num);
        for (int i = 0; i < num[0]; i++) {
            cl_device_id[] devices = new cl_device_id[10];
            int[] numDev = new int[1];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL,
                    devices.length, devices, numDev);
            for (int j = 0; j < numDev[0]; j++) {
                if (devices[j].equals(device)) {
                    return platforms[i];
                }
            }
        }
        throw new RuntimeException("Device not found in any platform");
    }
    public void initBuffers() {
        dxMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * TextureFinder.N, Pointer.to(TextureFinder.dx), null);
        dyMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * TextureFinder.N, Pointer.to(TextureFinder.dy), null);
        dzMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * TextureFinder.N, Pointer.to(TextureFinder.dz), null);
        expectedMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * TextureFinder.N, Pointer.to(TextureFinder.expected), null);
        outXMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,Sizeof.cl_int * MAX_OUT, null, null);
        outZMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,Sizeof.cl_int * MAX_OUT, null, null);
        //outIMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,Sizeof.cl_int * MAX_OUT, null, null);
        //outValMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,Sizeof.cl_int * MAX_OUT, null, null);
        counterArr = new int[]{0};
        counterMem = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,Sizeof.cl_int,Pointer.to(counterArr),null);
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(dxMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(dyMem));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(dzMem));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(expectedMem));
        clSetKernelArg(kernel, 7, Sizeof.cl_int, Pointer.to(new int[]{TextureFinder.N}));
        clSetKernelArg(kernel, 10, Sizeof.cl_int, Pointer.to(new int[]{MAX_OUT}));
        Arrays.fill(clearX, Integer.MIN_VALUE);
        Arrays.fill(clearZ, Integer.MIN_VALUE);
    }
    // === 核心 API ===
    public List<Result> run(int d,int y) {
        counterArr[0] = 0;
        clEnqueueWriteBuffer(queue,counterMem,true,0,Sizeof.cl_int,Pointer.to(counterArr),0,null,null);
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(outXMem));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(outZMem));
        clEnqueueWriteBuffer(queue,outXMem,true,0,Sizeof.cl_int * MAX_OUT,Pointer.to(clearX),0,null,null);
        clEnqueueWriteBuffer(queue,outZMem,true,0,Sizeof.cl_int * MAX_OUT,Pointer.to(clearZ),0,null,null);
        //clSetKernelArg(kernel, 10, Sizeof.cl_mem, Pointer.to(outIMem));
        //clSetKernelArg(kernel, 11, Sizeof.cl_mem, Pointer.to(outValMem));
        clSetKernelArg(kernel, 9, Sizeof.cl_int, Pointer.to(new int[]{d}));
        clSetKernelArg(kernel, 8, Sizeof.cl_int, Pointer.to(new int[]{y}));
        clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(counterMem));
        long total = 8L * d;
        //System.out.println("total="+total);
        // === 執行 ===
        long[] globalWorkSize = new long[]{total};

        clEnqueueNDRangeKernel(queue, kernel, 1, null,globalWorkSize, null, 0, null, null);
        // === 讀回結果 ===
        clEnqueueReadBuffer(queue,counterMem,true,0,Sizeof.cl_int,Pointer.to(counterArr),0,null,null);
        int found = counterArr[0];
        //System.out.println(getid()+": "+found);
        found=Math.min(found, MAX_OUT);
        //System.out.println("found="+found);
        if (found == 0) {
            return Collections.emptyList();
        }
        int[] outXArr = new int[found];
        int[] outZArr = new int[found];
        //int[] outI = new int[found];
        //int[] outVal = new int[found];
        clEnqueueReadBuffer(queue, outXMem, true, 0,Sizeof.cl_int * found, Pointer.to(outXArr), 0, null, null);
        clEnqueueReadBuffer(queue, outZMem, true, 0,Sizeof.cl_int * found, Pointer.to(outZArr), 0, null, null);
        //clEnqueueReadBuffer(queue, outIMem, true, 0,Sizeof.cl_int * found, Pointer.to(outI), 0, null, null);
        //clEnqueueReadBuffer(queue, outValMem, true, 0,Sizeof.cl_int * found, Pointer.to(outVal), 0, null, null);
        List<Result> results = new ArrayList<>(found);
        for (int i = 0; i < found; i++) {
            results.add(new Result(outXArr[i], outZArr[i]));
        }
        return results;
    }
    // === 讀 kernel ===
    private String loadKernel() {
        String clloc ="/"+getclname()+".cl";
        //System.out.println("try "+clloc);
        try (InputStream is = GpuRunner.class.getResourceAsStream(clloc)) {
            
            if (is == null) {
                throw new RuntimeException("Kernel file not found");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            //System.out.println("fail "+clloc);
            throw new RuntimeException(e);
        }
    }
    private String getclname() {
        TextureProvider mode = Main.mode;
        String s = "search";
        if(mode instanceof VanillaTextures){
            return s+"Vanilla";
        }
        else if(mode instanceof Vanilla12Textures){
            return s+"Vanilla12";
        }
        else if(mode instanceof Vanilla21_1Textures va){
            if(va instanceof Sodium19Textures so){
                if(so instanceof SodiumTextures){
                    return s+"Sodium";
                }
                return s+"Sodium19";
            }
            else{
                return s+"Vanilla21_1";
            }
        }
        else{
            return s;
        }
    }
}