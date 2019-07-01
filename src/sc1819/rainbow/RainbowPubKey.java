package sc1819.rainbow;

import sc1819.rainbow.util.CentralMap;
import sc1819.rainbow.util.GF16;
import sc1819.rainbow.util.Layer;
import sc1819.rainbow.util.MultQuad;

import java.io.*;

/**
 * This class represents the public key for the Rainbow Signature Scheme.
 * <p>
 * A public key consists of a single map given by the composition 
 * {@code P=S} ° {@code F} ° {@code T}; where {@code T,S} and {@code F} are the maps of the corresponding private key.
 * <p>
 * The public map {@code P}  is a system of {@code m} multivariate quadratic polynomials in {@code n} variables. 
 * 
 */
public class RainbowPubKey implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The public map {@code P}, a system of multivariate quadratic polynomials, stored as an array of MultQuads.
	 */
	private MultQuad[] P;
	/**
	 * Parameters of this public key, {@code n} is the number of variables, {@code m} the number of equations.
	 */
	private int n, m;
	
	/**
	 * Constructor, generates the system of polynomials {@code P}, corresponding to the composition of the secret key maps.
	 * @param sk the secret key corresponding to this public key
	 */
	public RainbowPubKey(RainbowSecKey sk) {
		n = sk.getVarNum();
		m = sk.getEqNum();

        P = new MultQuad[m];

        //Composition of F and T
		byte[][] T = sk.getT().getMatrix();
		byte[] vt = sk.getT().getVector();
		CentralMap F = sk.getF();

		compositionOfFAndT(F, T, vt);

		//Composition of S and (F°T)
        byte[][] S = sk.getS().getMatrix();
        byte[] vs = sk.getS().getVector();

        compositionOfSAndFcompT(S, vs);
	}

	private void compositionOfFAndT(CentralMap f, byte[][] t, byte[] vt) {
		int vi, oi;
		byte[][] coeffQ;
		byte[] coeffL, coeffL1, coeffL2;
		byte coeffT;

		byte[] vtv, vto;

		byte[][] quad;
		byte[] lin;

		int index = 0;

		for (Layer layer : f.getLayers()) {
			vi = layer.getVi();
			oi = layer.getOi();

			vtv = new byte[vi]; // copy of Vinegar variables of vt
			System.arraycopy(vt, 0, vtv, 0, vi);

			vto = new byte[oi]; // copy of Oil variables of vt
			System.arraycopy(vt, vi, vto, 0, oi);

			for (MultQuad[] poly : layer.getPoly()) {
				//vv: Alpha
				quad = poly[0].getQuad();

				coeffQ = composeQuadraticWithMatrix(quad, t, 0, vi, 0, vi, n);

				coeffL1 = composeLinearWithMatrix(GF16.prodVectMat(vtv, quad), t, 0, vi, n);
				coeffL2 = composeMatrixWithLinear(GF16.prodMatVec(quad, vtv), t, 0, vi, n);
				coeffL = GF16.addVectors(coeffL1, coeffL2);

				coeffT = GF16.prodVecVec(GF16.prodVectMat(vtv, quad), vtv);

				P[index] = new MultQuad(coeffQ, coeffL, coeffT);

				//vo: Beta
				quad = poly[1].getQuad();

				coeffQ = composeQuadraticWithMatrix(quad, t, 0, vi, vi, vi + oi, n);

				coeffL1 = composeLinearWithMatrix(GF16.prodVectMat(vtv, quad), t, vi, vi + oi, n);
				coeffL2 = composeMatrixWithLinear(GF16.prodMatVec(quad, vto), t, 0, vi, n);
				coeffL = GF16.addVectors(coeffL1, coeffL2);

				coeffT = GF16.prodVecVec(GF16.prodVectMat(vtv, quad), vto);

				P[index] = MultQuad.combine(P[index], new MultQuad(coeffQ, coeffL, coeffT));

				//vv:term + vv:lin + o-lin
				//vv:lin
				lin = poly[0].getLin();

				coeffQ = new byte[n][n];

				coeffL1 = composeLinearWithMatrix(lin, t, 0, vi, n);
				coeffT = GF16.prodVecVec(lin, vtv);

				//o-lin
				lin = poly[2].getLin();

				coeffL2 = composeLinearWithMatrix(lin, t, vi, vi + oi, n);
				coeffL = GF16.addVectors(coeffL1, coeffL2);

				coeffT = GF16.add(coeffT, GF16.prodVecVec(lin, vto));

				//vv:term
				coeffT = GF16.add(coeffT, poly[0].getTerm());

				P[index] = MultQuad.combine(P[index], new MultQuad(coeffQ, coeffL, coeffT));

				index++;
			}
		}
	}

    private void compositionOfSAndFcompT(byte[][] s, byte[] vs) {
        MultQuad poly;
        MultQuad[] polyArray = new MultQuad[m];
        //Se y è il vettore dei polinomi e la mappa affine è data da S+v
        //il prodotto Sy manderà ogni polinomio in una combinazione lineare di polinomi
        //ci basterà quindi un metodo che mandi un polinomio in un suo multiplo per poi usare
        //il metodo combine di MultQuad

        //Applico S
        for (int i = 0; i < m; i++) {
            poly = MultQuad.mult(P[0], s[i][0]);
            for (int j = 1; j < m; j++) {
                poly = MultQuad.combine(poly, MultQuad.mult(P[j], s[i][j]));
            }

            polyArray[i] = poly;
        }

        //Applico vs
        for (int i = 0; i < m; i++) {
            polyArray[i].addTerm(vs[i]);
            P[i] = polyArray[i];
        }
    }

	private byte[][] composeQuadraticWithMatrix(byte[][] quad, byte[][] affineMat, int startRow, int endRow, int startCol, int endCol, int mapSize) {
		byte[][] res = new byte[mapSize][mapSize];
		byte colFactor;
		int rowIndex, colIndex;

		for (int i = 0; i < mapSize; i++) {
			for (int j = 0; j < mapSize; j++) {
				for (int k = startCol; k < endCol; k++) {
					colFactor = 0;

					for (int h = startRow; h < endRow; h++) {
						rowIndex = h - startRow;
						colIndex = k - startCol;

						colFactor = GF16.add(colFactor, GF16.mult(affineMat[h][i], quad[rowIndex][colIndex]));
					}

					res[i][j] = GF16.add(res[i][j], GF16.mult(colFactor, affineMat[k][j]));
				}
			}
		}

		return  res;
	}

	private byte[] composeLinearWithMatrix(byte[] lin, byte[][] affineMat, int start, int end, int mapSize) {
		byte[] res = new byte[mapSize];
		int index;

		for (int j = 0; j < mapSize; j++) {
			for (int i = start; i < end; i++) {
				index = i - start;
				res[j] = GF16.add(res[j], GF16.mult(lin[index], affineMat[i][j]));
			}
		}

		return res;
	}

	private byte[] composeMatrixWithLinear(byte[] lin, byte[][] affineMat, int start, int end, int mapSize) {
		byte[] res = new byte[mapSize];
		int index;

		for (int i = 0; i < mapSize; i++) {
			for (int j = start; j < end; j++) {
				index = j - start;
				res[i] = GF16.add(res[i], GF16.mult(affineMat[j][i], lin[index]));
			}
		}

		return res;
	}

	/**
	 * Returns the evaluation of this public map on an array of field elements.
	 * @param x the array on which the map is evaluated
	 * @return the result of the evaluation
	 */
	public byte[] eval(byte[] x) {
		byte[] res = new byte[m];
		
		for (int i = 0; i < m; i++) {
			res[i] = P[i].eval(x,x);
		}
		
		return res;
	}
	
	/**
	 * Loads a public key from a file.
	 * 
	 * @param path the path of the file containing the key to be loaded
	 * @return the public key loaded from the file
	 */
	public static RainbowPubKey loadKey(String path) {
		RainbowPubKey pk = null;

		FileInputStream fin = null;
		ObjectInputStream ois = null;

		try {

			fin = new FileInputStream(path);
			ois = new ObjectInputStream(fin);
			pk = (RainbowPubKey) ois.readObject();
		} catch (FileNotFoundException ex) {
			System.out.println(path+" not found!");
			System.exit(1);
		} catch (ClassCastException | StreamCorruptedException ex) {
			System.out.println(path+" is not a valid public key!");
			System.exit(1);
		} catch (ClassNotFoundException | IOException ex) {
			ex.printStackTrace();
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
		
		return pk;
	}
	
	public static void main(String[] args) {

	}
}
