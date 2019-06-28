package sc1819.rainbow.util;

import java.io.Serializable;
import java.security.SecureRandom;


/**
 * This class represents a layer of quadratic equations for the Rainbow signature scheme's central map.
 * <p>
 * The coefficients of each polynomial of a layer are stored in three separate MultQuads, as multivariate quadratic polynomials.
 * <p>
 * This class also provides a method for evaluating the polynomials of the whole layer, as well as a method for evaluating a single polynomial.
 */
public class Layer implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The number of vinegar variables in this Layer.
     */
    private int vi;
    /**
     * The number of polynomials in this Layer, which corresponds to the number of oil variables.
     */
    private int oi;
    /**
     * Contains the polynomials of this Layer.
     */
    private MultQuad[][] poly;

    /**
     * Constructor, given the number of oil and vinegar variables, generates as many random field elements as necessary to build the polynomials of this layer.
     *
     * @param vi     the number of vinegar variables in this Layer.
     * @param oi     the number of oil variables in this Layer.
     * @param random the source of random field elements needed to generate the various coefficients
     */
    public Layer(int vi, int oi, SecureRandom random) {
        this.vi = vi;
        this.oi = oi;
        byte[][][] alpha = new byte[oi][vi][vi];
        byte[][][] beta = new byte[oi][vi][oi];
        byte[][] gammav = new byte[oi][vi];
        byte[][] gammao = new byte[oi][oi];
        byte[] delta = new byte[oi];
        poly = new MultQuad[oi][3];

        for (int i = 0; i < oi; i++) {
            for (int j = 0; j < vi; j++) {
                for (int k = 0; k < vi; k++) {
                    if (j <= k) alpha[i][j][k] = (byte) random.nextInt(16);
                }

                for (int k = 0; k < oi; k++) {
                    beta[i][j][k] = (byte) random.nextInt(16);
                }

                gammav[i][j] = (byte) random.nextInt(16);
            }

            for (int j = 0; j < oi; j++) {
                gammao[i][j] = (byte) random.nextInt(16);
            }

            delta[i] = (byte) random.nextInt(16);

            poly[i][0] = new MultQuad(alpha[i], gammav[i], delta[i]);
            poly[i][1] = new MultQuad(beta[i]);
            poly[i][2] = new MultQuad(gammao[i]);
        }
    }

    /**
     * Computes the result of evaluating a single polynomial, represented as a Layer polynomial.
     *
     * @param poly the polynomial that is to be evaluated
     * @param x    the array of field elements on which the polynomial is evaluated
     * @return the result of the evaluation
     */
    public byte polyEval(MultQuad[] poly, byte[] x) {
        byte[] xv = new byte[vi];
        byte[] xo = new byte[oi];

        System.arraycopy(x, 0, xv, 0, vi);
        System.arraycopy(x, vi, xo, 0, oi);

        byte res = poly[0].eval(xv, xv);
        res = GF16.add(res, poly[1].eval(xv, xo));
        res = GF16.add(res, poly[2].eval(xo));

        return res;
    }

    /**
     * Computes the evaluation of each polynomial of this layer on an array of field elements.
     *
     * @param x the array on which the polynomials are evaluated
     * @return an array containing the result of each evaluation
     */
    public byte[] eval(byte[] x) {
        byte[] res = new byte[oi];

        for (int i = 0; i < oi; i++) {
            res[i] = polyEval(poly[i], x);
        }

        return res;
    }

    /**
     * Returns all the polynomials of this Layer.
     *
     * @return a matrix containing all the polynomials of this layer
     */
    public MultQuad[][] getPoly() {
        return poly;
    }

    /**
     * Returns the number of vinegar variables in this Layer.
     *
     * @return the number of vinegar variables
     */
    public int getVi() {
        return vi;
    }

    /**
     * Returns the number of oil variables in this Layer.
     *
     * @return the number of oil variables
     */
    public int getOi() {
        return oi;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
