package sc1819.rainbow.util;

import java.io.Serializable;

/**
 * This class is used to represent multivariate quadratic polynomials, 
 * as the sum of a quadratic component, a linear component and a free term.
 * <p>
 * MultQuad objects are typed as: 
 * <ul>
 * <li>
 * {@code 0} if total, consisting of both a linear and quadratic component as well as a free term;
 * </li>
 * <li>
 * {@code 1} if consisting of only a quadratic component;
 * </li>
 * <li>
 * {@code 2} if consisting of only a linear component.
 * </li>
 * </ul>
 * <p>
 * This class provides a different constructor for each type of MultQuad, where the linear component, quadratic component and free term are stored separately in appropriately sized structures as matrices, vectors or single elements of GF16.
 * <p>
 * This class also provides multiple {@code eval} methods, for differently typed MultQuads.
 */
public class MultQuad implements Serializable{
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * A matrix of elements of GF16, containing the coefficients of the quadratic component of this MultQuad.
	 */
	private byte[][] quad;
	
	/**
	 * An array of elements of GF16, containing the coefficients of the linear component of this MultQuad.
	 */
	private byte[] lin;
	
	/**
	 * An element of GF16, representing the free term of this MultQuad.
	 */
	private byte term;

	/**
	 * <p>
	 * A label telling us how this MultQuad is composed.
	 * <ul>
	 * <li>
	 * {@code type=0} if this MultQuad is total, consisting of both a linear and quadratic component as well as a free term;
	 * </li>
	 * <li>
	 * {@code type=1} if this MultQuad consists of only a quadratic component;
	 * </li>
	 * <li>
	 * {@code type=02} if this MultQuad consists of only a linear component.
	 * </li>
	 * </ul>
	 */
	private int type; //0 total; 1 onlyQuad; 2 onlyLin
	
	
	/**
	 * Constructor, builds a MultQuad of type=0, that is with all components, from a matrix of quadratic coefficients, an array of linear coefficients and a free term.
	 * 
	 * @param quad the matrix representing the coefficients of the quadratic component 
	 * @param lin the array representing the coefficients of the linear component 
	 * @param term the field element representing the free term
	 */
	public MultQuad(byte[][] quad, byte[] lin, byte term) {
		this.type = 0;
		this.quad = quad;
		this.lin = lin;
		this.term = term;
	}
	
	/**
	 * Constructor, builds a MultQuad of type=1, that is only with a quadratic component, from a matrix containing the quadratic coefficients.
	 * 
	 * @param quad the matrix containing the coefficients of the quadratic component 
	 */
	public MultQuad(byte[][] quad) {
		this.type = 1;
		this.quad = quad;
	}
	
	/**
	 * Constructor, builds a MultQuad of type=2, that is only with a linear component, from an array containing the linear coefficients.
	 * 
	 * @param lin the array containing the coefficients of the linear component 
	 */
	public MultQuad(byte[] lin) {
		this.type = 2;
		this.lin = lin;
	}

	/**
	 * Returns the result of evaluating the linear part of this MultQuad.
	 * 
	 * @param x1 the array of field elements on the evaluation is performed
	 * @return the result, an element of GF16, of evaluating the linear part of this MultQuad
	 */
	public byte eval(byte[] x1) {
		return GF16.prodVecVec(lin, x1);
	}
	
	/**
	 * Returns the result of evaluating this MultQuad, it's computed as follows:
	 * <ul>
	 * <li>
	 * if {@code type=0} the result is given by (x1*{@code this.quad})*x2+(x1*{@code this.lin})+{@code this.term};
	 * </li>
	 * <li>
	 * if {@code type=1} the result is given by (x1*{@code this.quad})*x2.
	 * </li>
	 * </ul>
	 * @param x1 the first array of field elements in the evaluation
	 * @param x2 the first array of field elements in the evaluation
	 * @return the result of the computation
	 */
	public byte eval(byte[] x1, byte[] x2) {
		byte scal1 = GF16.prodVecVec(GF16.prodVectMat(x1, quad), x2);
		
		if (type == 1) {
			return scal1;
		}
		
		byte scal2;
		
		scal2 = GF16.prodVecVec(x1, lin);
		
		return GF16.add(scal1, GF16.add(scal2, term));
	}
	
	
	/**
	 * Combines two MultQuads by adding their coefficients one by one.
	 * 
	 * @param P the first MultQuad to be combined
	 * @param Q the second MultQuad to be combined
	 * @return a new MultQuad such that its coefficients are equal to the sum of the respective coefficients of P and Q
	 */
	public static MultQuad combine(MultQuad P, MultQuad Q) {
		byte[][] coeffQ;
		byte[] coeffL;
		byte coeffT;
		byte[][] PQuad = P.getQuad();
		byte[][] QQuad = Q.getQuad();
		byte[] PLin = P.getLin();
		byte[] QLin = Q.getLin();
		
		if (PQuad.length != QQuad.length || PQuad[0].length != QQuad[0].length || PLin.length != QLin.length) {
			throw new IllegalArgumentException("MultQuad di dim diverse!");
		}
		coeffQ = new byte[PQuad.length][PQuad[0].length];
		coeffL = new byte[PLin.length];
		
		for (int i = 0; i < PQuad.length; i++) {
			coeffL[i] = GF16.add(PLin[i], QLin[i]);
			
			for (int j = 0; j < PQuad[0].length; j++) {
				coeffQ[i][j] = GF16.add(PQuad[i][j], QQuad[i][j]);
			}
		}
		
		coeffT = GF16.add(P.getTerm(), Q.getTerm());
		return new MultQuad(coeffQ, coeffL, coeffT);
	}
	
	/**
	 * Given MultQuad and a field element this method returns a new MultQuad after multiplying each coefficient by the field element.
	 * 
	 * @param P the MultQuad we want to multiply
	 * @param a the field element by which we want to multiply the MultQuad
	 * @return  a MultQuad such that its coefficients are equal to those of P multiplied by a field element.
	 */
	public static MultQuad mult(MultQuad P, byte a) {
		byte[][] PQuad = P.getQuad();
		byte[] PLin = P.getLin();
		byte[][] coeffQ = new byte[PQuad.length][PQuad[0].length];
		byte[] coeffL = new byte[PLin.length];
		byte coeffT;
		
		for (int i = 0; i < PQuad.length; i++) {
			coeffL[i] = GF16.mult(a, PLin[i]);
			
			for (int j = 0; j < PQuad[0].length; j++) {
				coeffQ[i][j] = GF16.mult(a, PQuad[i][j]);
			}
		}
		
		coeffT = GF16.mult(a, P.getTerm());
		
		return new MultQuad(coeffQ, coeffL, coeffT);
	}
	

	/**
	 * Performs an addition on the free term of this Multquad.
	 * 
	 * @param v the field element that is to be added to the free term
	 */
	public void addTerm(byte v) {
		term = GF16.add(term, v);
	}
	
	/**
	 * Returns the quadratic part of this MultQuad.
	 * 
	 * @return the {@code quad} of this MultQuad
	 */
	public byte[][] getQuad() {
		return quad;
	}
	
	/**
	 * Returns the linear part of this MultQuad.
	 * 
	 * @return the {@code lin} this MultQuad
	 */
	public byte[] getLin() {
		return lin;
	}
	
	/**
	 * Returns the free term of this MultQuad.
	 * 
	 * @return the {@code term} of this MultQuad
	 */
	public byte getTerm() {
		return term;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
