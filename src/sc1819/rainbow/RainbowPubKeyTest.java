package sc1819.rainbow;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sc1819.rainbow.RainbowSecKey;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RainbowPubKeyTest {
    private static String skPath;
    private static String pkPath;

    @BeforeAll
    public static void setup() {
        skPath = "sk_test.txt";
        pkPath = "pk_test.txt";
    }


    @Test
    public void testCreatePublicKey() throws Exception {
        RainbowSecKey loadedSecKey = RainbowSecKey.loadKey(skPath);
        RainbowPubKey loadedPubKey = RainbowPubKey.loadKey(pkPath);

        assertNotNull(loadedSecKey, "expected existing sec key");
        assertNotNull(loadedPubKey, "expected existing pub key");

        RainbowPubKey pubKey = new RainbowPubKey(loadedSecKey);

        assertTrue(assertPubKeyEquals(pubKey, loadedPubKey, loadedSecKey.getVarNum()));
    }

    private boolean assertPubKeyEquals(RainbowPubKey pubKey1, RainbowPubKey pubKey2, int n) {
        byte[] x = new byte[n];
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < n; j++)
                x[j] = (byte) random.nextInt(16);

            if (!Arrays.equals(pubKey1.eval(x), pubKey2.eval(x)))
                return false;
        }

        return true;
    }
}