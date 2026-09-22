package com.nest.kanxue.devicefingerprint;

public class DrmIdResult {
    private final byte[] data;
    private final long length;

    public DrmIdResult(byte[] data, long length) {
        this.data = data;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public long getLength() {
        return length;
    }
} 