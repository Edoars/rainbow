package sc1819.rainbow.debug;

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Create a SecureRandom wich produces the same values.
 * <b>This is for testing only!</b>
 */
public class FixedRand extends SecureRandom {
    MessageDigest sha;
    byte[] state;

    public FixedRand() {
        try {
            this.sha = MessageDigest.getInstance("SHA-1");
            this.state = sha.digest();
        } catch (Exception e) {
            throw new RuntimeException("can't find SHA-1!");
        }
    }

    @Override
    public void nextBytes(byte[] bytes) {
        int off = 0;
        sha.update(state);
        while (off < bytes.length) {
            state = sha.digest();
            if (bytes.length - off > state.length) {
                System.arraycopy(state, 0, bytes, off, state.length);
            } else {
                System.arraycopy(state, 0, bytes, off, bytes.length - off);
            }
            off += state.length;
            sha.update(state);
        }
    }

    @Override
    public int nextInt(int bound) {
        byte[] next = new byte[1];
        nextBytes(next);

        return (next[0] & 0xFF) % bound;
    }
}
