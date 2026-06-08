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
    int d
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
    for (int i = 0; i < N; i++) {
        int tx = x + dx[i];
        int ty = y + dy[i];
        int tz = z + dz[i];
        long seed = (long)(tx * 3129871) ^ (long)(tz * 116129781L) ^ (long)ty;
        seed = seed * seed * 42317861L + seed * 11L;
        seed = seed >> 16;
        ulong tmp = (ulong) seed;
        seed = seed ^ (tmp >> 33);
        seed = seed * 0xff51afd7ed558ccdL;
        tmp = (ulong)seed;
        seed = seed ^ (tmp >> 33);
        seed = seed * 0xc4ceb9fe1a85ec53L;
        tmp = (ulong)seed;
        seed = seed ^ (tmp >> 33);
        seed = seed + 0x9E3779B97F4A7C15L;
        tmp = (ulong)seed;
        long rand1 = seed ^ (tmp >> 30);
        rand1 = rand1 * 0xBF58476D1CE4E5B9L;
        tmp = (ulong)rand1;
        rand1 = rand1 ^ (tmp >> 27);
        rand1 = rand1 * 0x94D049BB133111EBL;
        tmp = (ulong)rand1;
        rand1 = rand1 ^ (tmp >> 31);
        long rand2 = seed + 0x9E3779B97F4A7C15L;
        tmp = (ulong)rand2;
        rand2 = rand2 ^ (tmp >> 30);
        rand2 = rand2 * 0xBF58476D1CE4E5B9L;
        tmp = (ulong)rand2;
        rand2 = rand2 ^ (tmp >> 27);
        rand2 = rand2 * 0x94D049BB133111EBL;
        tmp = (ulong)rand2;
        rand2 = rand2 ^ (tmp >> 31);
        int next = (int)(rand1 + rand2);
        next = next < 0 ? -next : next;
        int val = next % 4;
        if (val != expected[i]) {
            return;
        }
    }
    int idx = atomic_inc(counter);
    if (idx < 400) {
        outX[idx] = x;
        outZ[idx] = z;
    }
}