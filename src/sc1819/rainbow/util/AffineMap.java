package sc1819.rainbow.util;

import java.io.Serializable;
import java.security.SecureRandom;

/**
 * This class represents an affine map in G16, consisting of a matrix part M and a vectorial part v, hence evaluating such a map on x would mean computing Mx+v.
 * <p>
 * The matrix and vector part are stored separately, as well as the matricial component of the inverse.
 * <p>
 * This class also provides a method for evaluating the map on a vector of G16 elements.
 */
public class AffineMap implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Contains the matrix part of this affine map.
     */
    private byte[][] matrix;
    /**
     * Contains the inverse of the matrix representing this affine map.
     */
    private byte[][] inverse;
    /**
     * Contains the vector part of this affine map.
     */
    private byte[] vector;

    /**
     * Constructor, builds an affine map as follows:
     * generates a {@code size}x{@code size} matrix of random field elements until this has valid inverse,
     * then generates a {@code size}-long vector of random field elements.
     *
     * @param size   the dimension of the {@code size}x{@code size} matrix and length of the vector which represent the AffineMap we're constructing
     * @param random needed to generate random coefficients from F16
     */
    public AffineMap(int size, SecureRandom random) {
        this.matrix = new byte[size][size];
        this.inverse = null;

        while (inverse == null) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = (byte) random.nextInt(16);
                }
            }

            inverse = GF16.matrixInverse(matrix);
        }

        this.vector = new byte[size];
        for (int i = 0; i < size; i++) {
            vector[i] = (byte) random.nextInt(16);
        }
    }

    /**
     * This method evaluates this affine map on a vector of appropriate length,
     * computing {@code this.matrix}*{@code x}+{@code this.vector}.
     *
     * @param x the vector on which we wish to evaluate this affine map
     * @return result of the evaluation of x
     * @throws IllegalArgumentException if x's length does not match the matrix's dimensions
     */
    public byte[] eval(byte[] x) {
        if (matrix.length != x.length) {
            throw new IllegalArgumentException("Wrong dimensions!");
        }
        byte[] res;

        res = GF16.prodMatVec(matrix, x);

        for (int i = 0; i < res.length; i++) {
            res[i] = GF16.add(res[i], vector[i]);
        }
        return res;
    }

    /**
     * This method evaluates the inverse of this affine map on a vector of appropriate length,
     * computing {@code this.inverse}*({@code x}+{@code this.vector}).
     *
     * @param x the vector on which we wish to evaluate the inverse
     * @return result of the evaluation of x
     * @throws IllegalArgumentException if x's length does not match the inverse matrix's dimensions
     */
    public byte[] evalInv(byte[] x) {
        if (inverse.length != x.length) {
            throw new IllegalArgumentException("Wrong dimensions!");
        }
        byte[] res = new byte[inverse.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = GF16.add(x[i], vector[i]);
        }

        res = GF16.prodMatVec(inverse, res);

        return res;
    }

    /**
     * Returns the matrix representing the inverse of this map
     *
     * @return the inverse of this affine map
     */
    public byte[][] getInverse() {
        return inverse;
    }

    /**
     * Returns the matrix part of this affine map
     *
     * @return the matrix of this affine map
     */
    public byte[][] getMatrix() {
        return matrix;
    }

    /**
     * Returns the vectorial part of this affine map
     *
     * @return the vector of this affine map
     */
    public byte[] getVector() {
        return vector;
    }
}
