package sc1819.rainbow;

import sc1819.rainbow.util.AffineMap;
import sc1819.rainbow.util.CentralMap;

import java.io.*;
import java.security.SecureRandom;

/**
 * This class represents the secret key for the Rainbow Signature Scheme.
 * <p>
 * A secret key consists of:
 * <ul>
 * <li>
 * two affine maps {@code T}:(GF16)<sup>n</sup>-{@literal >}(GF16)<sup>n</sup> and {@code S}:(GF16)<sup>m</sup>-{@literal >}(GF16)<sup>m</sup>;
 * </li>
 * <li>
 * a central map {@code F}:(GF16)<sup>n</sup>-{@literal >}(GF16)<sup>m</sup>.
 * </li>
 * </ul>
 * This class also provides a method for loading a secret key from a file.
 */
class RainbowSecKey implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * The affine maps of this secret key.
     */
    private AffineMap S, T;
    /**
     * The central map of this secret key.
     */
    private CentralMap F;
    /**
     * The number of variables involved in this key.
     */
    private byte n; //Numero variabili
    /**
     * The number of equations involved in this key.
     */
    private byte m; //Numero equazioni

    /**
     * Constructor, sets the parameters {@code n} and {@code m} and generates all the maps.
     *
     * @param param  the parameters of the scheme
     * @param random the source of random field elements needed to generate all the maps
     */
    public RainbowSecKey(RainbowParameters param, SecureRandom random) {
        this.m = (byte) (param.geto1() + param.geto2());
        this.n = (byte) (this.m + param.getv1());

        this.S = new AffineMap(m, random);

        this.T = new AffineMap(n, random);

        this.F = new CentralMap(param.getv1(), param.geto1(), param.geto2(), random);
    }

    /**
     * Loads a secret key from a file.
     *
     * @param path the path of the file containing the key to be loaded
     * @return the secret key loaded from the file
     */
    public static RainbowSecKey loadKey(String path) {
        RainbowSecKey sk = null;

        FileInputStream fin = null;
        ObjectInputStream ois = null;

        try {
            fin = new FileInputStream(path);
            ois = new ObjectInputStream(fin);
            sk = (RainbowSecKey) ois.readObject();
        } catch (FileNotFoundException ex) {
            System.out.println(path + " not found!");
            System.exit(1);
        } catch (ClassCastException | ClassNotFoundException | IOException ex) {
            System.out.println(path + " is not a valid private key!");
            System.exit(1);
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sk;
    }

    /**
     * Returns the number of equations in this key.
     *
     * @return m
     */
    public byte getEqNum() {
        return m;
    }

    /**
     * Returns the number of variables in this key.
     *
     * @return n
     */
    public byte getVarNum() {
        return n;
    }

    /**
     * Returns the first affine map of this key.
     *
     * @return S
     */
    public AffineMap getS() {
        return S;
    }

    /**
     * Returns the second affine map of this key.
     *
     * @return T
     */
    public AffineMap getT() {
        return T;
    }

    /**
     * Returns the inverse of the first affine map of this key.
     *
     * @return the inverse of S
     */
    public byte[][] getInvS() {
        return S.getInverse();
    }

    /**
     * Returns the vectorial component of the first affine map of this key.
     *
     * @return the vectorial part of S
     */
    public byte[] getCs() {
        return S.getVector();
    }

    /**
     * Returns the inverse of the second affine map of this key.
     *
     * @return the inverse of T
     */
    public byte[][] getInvT() {
        return T.getInverse();
    }

    /**
     * Returns the vectorial component of the second affine map of this key.
     *
     * @return the vectorial part of t
     */
    public byte[] getCt() {
        return T.getVector();
    }


    /**
     * Returns the central map of this key.
     *
     * @return F
     */
    public CentralMap getF() {
        return F;
    }
}
