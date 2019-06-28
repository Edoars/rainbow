package sc1819.rainbow.util;

import java.io.Serializable;
import java.security.SecureRandom;

/**
 * This class represents a central map for the private key of the Rainbow signature scheme.
 * <p>
 * A central map consist of two layers, each one is a system of multivariate polynomial equations.
 * The first layer has o1 equations in v1 vinegar variables and o1 oil variables.
 * The second layer has o2 equations in v2=v1+o1=n-o1 vinegar variables and o2 oil variables.
 * <p>
 * This class also provides a method for evaluating a central map on an array of field elements and a method for retrieving an x such that F(x)=y given y and being F a central map.
 */
public class CentralMap implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Contains the two layers of this central map.
     */
    private Layer[] layers = new Layer[2];
    /**
     * The parameters of this central map.
     */
    private int v1, o1, o2;

    /**
     * Constructor, sets up the parameters v1,o1,o2 and initializes the two layers of this central map.
     *
     * @param v1     the number of vinegar variables of the first layer of this central map
     * @param o1     the number of oil variables and of polynomials of the first layer of this central map
     * @param o2     the number of oil variables and of polynomials of the first layer of this central map
     * @param random the source of random field elements needed to initialize the layers
     */
    public CentralMap(int v1, int o1, int o2, SecureRandom random) {
        this.v1 = v1;
        this.o1 = o1;
        this.o2 = o2;

        layers[0] = new Layer(v1, o1, random);
        layers[1] = new Layer(v1 + o1, o2, random);
    }

    public static void main(String[] args) {
		/*SecureRandom random = new SecureRandom();
		CentralMap F = new CentralMap(1, 1, 1, random);
		int m = 2, n = 3;
		byte[] y = new byte[m];
		byte[] x;
		int c = 1;
		while (true) {
			for (int i = 0; i < m; i++) {
				y[i] = (byte)random.nextInt(16);
			}
			x = F.invF(y);
			if (Arrays.equals(F.eval(x), y)) {
				System.out.println(c+": Ok");
			} else {
				System.out.println(c+": False!");
				Debug d = new Debug();
				d.centralMapEval(F, 9, x);
				System.out.println("y: "+Arrays.toString(y));
				System.out.println("x: "+Arrays.toString(x));
				System.out.println("f(x): "+Arrays.toString(F.eval(x)));
				break;
			}
			c++;
		}*/
    }

    /**
     * Returns the result of evaluating the central map on an array of field elements.
     *
     * @param x the array on which the map is to be evaluated
     * @return the result of the evaluation
     */
    public byte[] eval(byte[] x) {
        byte[] x1, x2, res1, res2, res;
        x1 = new byte[v1 + o1];
        x2 = new byte[x.length];

        System.arraycopy(x, 0, x1, 0, v1 + o1);
        System.arraycopy(x, 0, x2, 0, x.length);

        res = new byte[o1 + o2];
        res1 = layers[0].eval(x1);
        res2 = layers[1].eval(x2);

        System.arraycopy(res1, 0, res, 0, o1);
        System.arraycopy(res2, 0, res, o1, o2);

        return res;
    }

    /**
     * Returns the layers of this central map.
     *
     * @return the layers of this central map
     */
    public Layer[] getLayers() {
        return layers;
    }

    /**
     * Returns an array x such that, given input y and being F this central map then y=F(x).
     *
     * @param y the result of an evaluation of this map
     * @return an array such that when this map is evaluated on it the result is {@code input}
     */
    public byte[] invF(byte[] y) {
        SecureRandom random = new SecureRandom();

        byte[] randomVector = new byte[v1];

        byte[] resOil1;
        byte[] resOil2 = null;

        byte[] partialSolution = new byte[v1 + o1];
        byte[] solution = new byte[v1 + o1 + o2];

        byte[] y1 = new byte[o1];
        byte[] y2 = new byte[o2];

        System.arraycopy(y, 0, y1, 0, y1.length);
        System.arraycopy(y, y1.length, y2, 0, y2.length);

        // repeat until we find a solution
        do {
            // generate a random vector of length v1
            for (int i = 0; i < v1; i++) {
                randomVector[i] = (byte) random.nextInt(16);
            }

            // try to solve the system
            // the solution would be the oil variables of the first layer
            resOil1 = getLinearSystemSolution(layers[0], randomVector, y1);

            // if a solution is found try to solve the second layer
            if (resOil1 != null) {
                System.arraycopy(randomVector, 0, partialSolution, 0, randomVector.length);
                System.arraycopy(resOil1, 0, partialSolution, randomVector.length, resOil1.length);

                // try to solve the system
                // the solution would be the oil variables of the second layer
                resOil2 = getLinearSystemSolution(layers[1], partialSolution, y2);
            }
        } while (resOil2 == null);

        System.arraycopy(partialSolution, 0, solution, 0, partialSolution.length);
        System.arraycopy(resOil2, 0, solution, partialSolution.length, resOil2.length);

        return solution;
    }

    private byte[] getLinearSystemSolution(Layer layer, byte[] partialSolution, byte[] input) {
        int oi = layer.getOi();
        int vi = layer.getVi();

        byte[][] matrix = new byte[oi][oi];
        byte[] matrixRow;
        byte[] vector = new byte[vi];

        MultQuad[][] layerPolynomials = layer.getPoly();
        MultQuad[] poly;

        // assemble the linear system by evaluating the layer with the partial solution
        for (int i = 0; i < oi; i++) {
            poly = layerPolynomials[i];

            // evaluate the vingard part of the polynomial
            vector[i] = GF16.add(poly[0].eval(partialSolution, partialSolution), input[i]);

            // evaluate the mixed part
            matrixRow = GF16.addVectors(
                    GF16.prodVectMat(partialSolution, poly[1].getQuad()),
                    poly[2].getLin()
            );

            System.arraycopy(matrixRow, 0, matrix[i], 0, matrixRow.length);
        }

        return GF16.SolveSys(matrix, vector);
    }

}
