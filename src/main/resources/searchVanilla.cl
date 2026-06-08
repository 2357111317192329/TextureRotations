__kernel void search(
    __global const int* dx,
    __global const int* dy,
    __global const int* dz,
    __global const int* expected,
    __global int* outX,
    __global int* outZ,
    __global int* counter,
    int N,
    int y,
    int d,
    int max
) {
    int gid = get_global_id(0);
    int side = gid / (2 * d);
    int offset = gid % (2 * d);
    int x, z;
    switch (side) {
        // 上邊
        case 0:
            x = -d + offset;
            z = d;
            break;
        // 右邊
        case 1:
            x = d;
            z = d - offset;
            break;
        // 下邊
        case 2:
            x = d - offset;
            z = -d;
            break;
        // 左邊
        default:
            x = -d;
            z = -d + offset;
            break;
    }
    //x=-950;
    //y=94;
    //z=-870;
    for (int i = 0; i < N; i++) {
        int tx = x + dx[i];
        int ty = y + dy[i];
        int tz = z + dz[i];
        long seed = (long)(tx * 3129871) ^ (long)(tz * 116129781L) ^ (long)ty;
        seed = seed * seed * 42317861L + seed * 11L;
        seed = seed >> 16;
        seed = (seed ^ 0x5DEECE66DL);
        seed = (seed * 0x5DEECE66DL + 11L) & ((1L << 48) - 1);
        int next = (int)(seed >> 17);
        int val = (next >> 29) % 4;
        //printf("i=%d seed=%ld next=%d val=%d expected=%d\n",i, seed, next, val,expected[i]);
        if (val != expected[i]) {
            return;
        }
    }
    int idx = atomic_inc(counter);
    if (idx < max) {
        outX[idx] = x;
        outZ[idx] = z;
    }
}