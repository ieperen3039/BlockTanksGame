package NG.Core;

import NG.Storable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Geert van Ieperen. Created on 19-9-2018.
 */
public class Version implements Comparable<Version>, Storable {
    private final int major;
    private final int minor;

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int major() {
        return major;
    }

    @Override
    public int compareTo(Version o) {
        return (major == o.major) ? Integer.compare(minor, o.minor) : Integer.compare(major, o.major);
    }

    public boolean isLessThan(int major, int minor) {
        return this.major == major ? this.minor < minor : this.major < major;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Version)) return false;
        Version other = (Version) obj;

        return major == other.major && minor == other.minor;
    }

    /**
     * checks whether this version is less than the given major and minor version, throwing an VersionMisMatch exception
     * if this is the case
     * @param major the major version required
     * @param minor the optional minor version, 0 if it doesn't matter
     * @throws MisMatchException if {@link this#isLessThan(int, int)}
     */
    public void requireAtLeast(int major, int minor) throws MisMatchException {
        if (this.isLessThan(major, minor)) {
            throw new MisMatchException(new Version(major, minor), this);
        }
    }

    @Override
    public String toString() {
        return "v " + major + "." + minor;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        out.writeChar('v');
        out.writeInt(major);
        out.writeInt(minor);
    }

    public Version(DataInputStream in) throws IOException {
        char c = in.readChar();
        if (c != 'v') {
            throw new IOException(String.format("Expected '%04x', but found %04x", (int) 'v', (int) c));
        }

        major = in.readInt();
        minor = in.readInt();
    }

    public static class MisMatchException extends Exception {
        public MisMatchException(Version required, Version current) {
            super("Version mismatch: required " + required + ", got " + current);
        }
    }
}
