package sc1819.rainbow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import sc1819.rainbow.util.*;

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
		
		P = new MultQuad[sk.getEqNum()];
		byte[][] T = sk.getT().getMatrix();
		byte[] vt = sk.getT().getVector();
		CentralMap F = sk.getF();
		
		
		//Composition of F and T
		int vi, oi;
		int pCount = 0;
		byte[][] quad;
		byte[] lin;
		byte[][] coeffQ;
		byte[] coeffL;
		byte[] coeffL2;
		byte coeffT;
		byte tmp;
		byte[] tmpV;
		MultQuad tmpP;
		
		byte[] vtv, vto;
		for (Layer layer : F.getLayers()) {
			vi = layer.getVi();
			oi = layer.getOi();
			
			vtv = new byte[vi]; //Copia di vt sulle variabili vinegar
			System.arraycopy(vt, 0, vtv, 0, vi);
			
			vto = new byte[oi];
			System.arraycopy(vt, vi, vto, 0, vi + oi - vi);
			
			for(MultQuad[] poly : layer.getPoly()) {
				//vv: Alpha
				coeffQ = new byte[n][n];
				quad = poly[0].getQuad();
				
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						for (int k = 0; k < vi; k++) {
							tmp = 0;
							
							for (int h = 0; h < k+1; h++) {
								tmp = GF16.add(tmp, GF16.mult(T[h][i], quad[h][k]));
							}
							
							coeffQ[i][j] = GF16.add(coeffQ[i][j], GF16.mult(tmp, T[k][j]));
						}
					}
				}
				
				tmpV = GF16.prodVectMat(vtv, quad);
				coeffL = new byte[n];
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < vi; j++) {
						coeffL[i] = GF16.add(coeffL[i], GF16.mult(tmpV[j], T[j][i]));
					}
				}
				
				tmpV = GF16.prodMatVec(quad, vtv);
				coeffL2 = new byte[n];
				for (int j = 0; j < n; j++) {
					for (int i = 0; i < vi; i++) {
						coeffL2[j] = GF16.add(coeffL2[j], GF16.mult(T[i][j], tmpV[i]));
					}
				}
				
				for (int i = 0; i < n; i++) {
					coeffL[i] = GF16.add(coeffL[i], coeffL2[i]);
				}
				
				coeffT = GF16.prodVecVec(GF16.prodVectMat(vtv, quad), vtv);
				
				P[pCount] = new MultQuad(coeffQ, coeffL, coeffT);
				
				//vo: beta
				coeffQ = new byte[n][n];
				quad = poly[1].getQuad();
				
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						for (int k = vi; k < vi+oi; k++) {
							tmp = 0;
							
							for (int h = 0; h < vi; h++) {
								tmp = GF16.add(tmp, GF16.mult(T[h][i], quad[h][k-vi]));
							}
							
							coeffQ[i][j] = GF16.add(coeffQ[i][j], GF16.mult(tmp, T[k][j]));
						}
					}
				}
				
				tmpV = GF16.prodVectMat(vtv, quad);
				coeffL = new byte[n];
				for (int i = 0; i < n; i++) {
					for (int j = vi; j < vi+oi; j++) {
						coeffL[i] = GF16.add(coeffL[i], GF16.mult(tmpV[j-vi], T[j][i]));
					}
				}
				
				tmpV = GF16.prodMatVec(quad, vto);
				coeffL2 = new byte[n];
				for (int j = 0; j < n; j++) {
					for (int i = 0; i < vi; i++) {
						coeffL2[j] = GF16.add(coeffL2[j], GF16.mult(T[i][j], tmpV[i]));
					}
				}
				
				for (int i = 0; i < n; i++) {
					coeffL[i] = GF16.add(coeffL[i], coeffL2[i]);
				}
				
				coeffT = GF16.prodVecVec(GF16.prodVectMat(vtv, quad), vto);
				
				tmpP = new MultQuad(coeffQ, coeffL, coeffT);
				P[pCount] = MultQuad.combine(P[pCount], tmpP);
				
				//vv:term + vv:lin + o-lin
				coeffQ = new byte[n][n];
				lin = poly[0].getLin();
				
				coeffL = new byte[n];
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < vi; j++) {
						coeffL[i] = GF16.add(coeffL[i], GF16.mult(lin[j], T[j][i]));
					}
				}
				
				/*tmpV = new byte[vi];
				for (int i = 0; i < vi; i++) {
					tmpV[i] = lin[i];
				}*/
				
				coeffT = GF16.prodVecVec(lin, vtv);
				
				lin = poly[2].getLin();
				for (int i = 0; i < n; i++) {
					for (int j = vi; j < vi+oi; j++) {
						coeffL[i] = GF16.add(coeffL[i], GF16.mult(lin[j-vi], T[j][i]));
					}
				}
				
				coeffT = GF16.add(coeffT, GF16.prodVecVec(lin, vto));
				
				//Aggiungo vv:term
				tmp = poly[0].getTerm();
				coeffT = GF16.add(coeffT, tmp);
				
				tmpP = new MultQuad(coeffQ, coeffL, coeffT);
				P[pCount] = MultQuad.combine(P[pCount], tmpP);
				
				pCount++;
			}
		}
		
		//Composition of S and (F°T)
		byte[][] S = sk.getS().getMatrix();
		byte[] vs = sk.getS().getVector();
		MultQuad[] tmpSP = new MultQuad[m];
		//Se y è il vettore dei polinomi e la mappa affine è data da S+v
		//il prodotto Sy manderà ogni polinomio in una combinazione lineare di polinomi
		//ci basterà quindi un metodo che mandi un polinomio in un suo multiplo per poi usare
		//il metodo combine di MultQuad
		
		//Applico S
		for (int i = 0; i < m; i++) {
			tmpP = MultQuad.mult(P[0], S[i][0]);
			for (int j = 1; j < m; j++) {
				tmpP = MultQuad.combine(tmpP, MultQuad.mult(P[j], S[i][j]));
			}
			
			tmpSP[i] = tmpP;
		}
		
		//Applico vs
		for (int i = 0; i < m; i++) {
			tmpSP[i].addTerm(vs[i]);
			P[i] = tmpSP[i];
		}
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
