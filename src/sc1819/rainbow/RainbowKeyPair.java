package sc1819.rainbow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;


/**
 * This class represents a key pair for the Rainbow signature scheme. It contains a secret key and its corresponding public key.
 * <p>
 * There are methods for loading and writing keys, from and to files, provided in this method.
 */
public class RainbowKeyPair {

    /**
     * The public key of this key pair.
     */
    private RainbowPubKey pk;
    /**
     * The private key of this key pair.
     */
    private RainbowSecKey sk;

    /**
     * Constructor, loads a secret key and a public key from two files.
     *
     * @param pkPath the path of the public key
     * @param skPath the path of the secret key
     * @throws IllegalStateException if either of the paths is null
     */
    public RainbowKeyPair(String pkPath, String skPath) {
        loadKeys(pkPath, skPath);

        if (pk == null || sk == null) {
            throw new IllegalStateException("Unable to load keys");
        }
    }

    /**
     * Constructor, given a source of random field elements, generates a key pair.
     *
     * @param random the source of random field elements
     */
    public RainbowKeyPair(RainbowParameters param, SecureRandom random) {
        this.sk = new RainbowSecKey(param, random);
        this.pk = new RainbowPubKey(sk);
    }

    /**
     * Loads both a public and a private key into this key pair.
     *
     * @param pkPath the path of the public key to be loaded
     * @param skPath the path of the secret key to be loaded
     */
    public void loadKeys(String pkPath, String skPath) {
        this.pk = RainbowPubKey.loadKey(pkPath);
        this.sk = RainbowSecKey.loadKey(skPath);
    }

    /**
     * Writes the private and secret key onto two files.
     *
     * @param pkPath the path of the file onto which the public key is to be written
     * @param skPath the path of the file onto which the private key is to be written
     */
    public void saveKeys(String pkPath, String skPath) {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            fout = new FileOutputStream(pkPath);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(pk);

            fout = new FileOutputStream(skPath);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(sk);

            //System.out.println("Done");

        } catch (Exception ex) {

            ex.printStackTrace();

        } finally {

            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Returns the private key f this key pair.
     *
     * @return the private key
     */
    public RainbowSecKey getSk() {
        return sk;
    }

    /**
     * Returns the public key f this key pair.
     *
     * @return the public key
     */
    public RainbowPubKey getPk() {
        return pk;
    }
}
