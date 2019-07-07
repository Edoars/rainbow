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
class RainbowKeyPair {

    /**
     * The public key of this key pair.
     */
    private RainbowPubKey pk;
    /**
     * The private key of this key pair.
     */
    private RainbowSecKey sk;
    /**
     * The parameters of this Rainbow key pair.
     */
    private RainbowParameters param = new RainbowParameters();

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
    public RainbowKeyPair(SecureRandom random) {
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

    public static void main(String[] args) {
        long time = System.nanoTime();
        RainbowKeyPair keys = new RainbowKeyPair(new SecureRandom());
        System.out.println("Tempo: " + (System.nanoTime() - time) / 1000);
        keys.saveKeys("pk.txt", "sk.txt");
        //RainbowKeyPair keys = new RainbowKeyPair("pk.txt", "sk.txt");
        RainbowSecKey sk = keys.sk;
        RainbowPubKey pk = keys.pk;
        //System.out.println("Chiavi generate");

        //System.out.println("Genero vettore casuale...");
        byte[] x = new byte[96];
        SecureRandom random = new SecureRandom();

        int c = 0;
		/*while(true) {
			for (int i = 0; i < 96; i++) {
				x[i] = (byte)random.nextInt(16);
			}
			
			AffineMap T = sk.getT();
			byte[] y = T.eval(x);
			
			byte[] pkEval = pk.eval(x);
			byte[] FEval1 = sk.getF().getLayers()[0].eval(y);
			byte[] FEval2 = sk.getF().getLayers()[1].eval(y);
			byte[] FEval = new byte[FEval1.length+FEval2.length];
			
			for (int i = 0; i < FEval1.length; i++) {
				FEval[i] = FEval1[i];
			}
			for (int i = 0; i < FEval2.length; i++) {
				FEval[i+FEval1.length] = FEval2[i];
			}
			
			AffineMap S = sk.getS();
			byte[] skEval = S.eval(FEval);
			
			System.out.print(c+": ");
			if (Arrays.equals(pkEval,skEval)) {
				System.out.println("Vero");
			} else {
				System.out.println("Falso");
				System.out.println(Arrays.toString(pkEval));
				System.out.println(Arrays.toString(skEval));
				break;
			}
			c++;
		}*/
    }

}
