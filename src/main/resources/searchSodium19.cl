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
        long l = seed ^ 7640891576956012809L;
        long m = l + -7046029254386353131L;
        ulong tmp = (ulong) l;
        l = l ^ (tmp >> 30);
        l = l * 0xBF58476D1CE4E5B9L;
        tmp = (ulong)l;
        l = l ^ (tmp >> 27);
        l = l * 0x94D049BB133111EBL;
        tmp = (ulong)l;
        l = l ^ (tmp >> 31);
        tmp = (ulong)m;
        m = m ^ (tmp >> 30);
        m = m * 0xBF58476D1CE4E5B9L;
        tmp = (ulong)m;
        m = m ^ (tmp >> 27);
        m = m * 0x94D049BB133111EBL;
        tmp = (ulong)m;
        m = m ^ (tmp >> 31);
        ulong r = rotate((ulong)(l + m), (ulong)17);
        int next = (int)(r + (ulong)l);
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