package sc1819.rainbow.util;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This class represents a central map for the private key of the Rainbow signature scheme.
 * <p>
 * A central map consist of two layers, each one is a system of multivariate polynomial equations.
 * The first layer has o1 equations in v1 vinegar variables and o1 oil variables.
 * The second layer has o2 equations in v2=v1+o1=n-o1 vinegar variables and o2 oil variables.
 * <p>
 * This class also provides a method for evaluating a central map on an array of field elements and a method for retrieving an x such that F(x)=y given y and being F a central map.
 */
public class CentralMap implements Serializable{
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Contains the two layers of this central map.
	 */
	private Layer[] layers = new Layer[2];
	/**
	 * The parameters of this central map.
	 */
	private int v1, o1, o2;
	
	/** Constructor, sets up the parameters v1,o1,o2 and initializes the two layers of this central map.
	 * @param v1 the number of vinegar variables of the first layer of this central map
	 * @param o1 the number of oil variables and of polynomials of the first layer of this central map
	 * @param o2 the number of oil variables and of polynomials of the first layer of this central map
	 * @param random the source of random field elements needed to initialize the layers
	 */
	public CentralMap(int v1, int o1, int o2, SecureRandom random) {
		this.v1 = v1;
		this.o1 = o1;
		this.o2 = o2;
		
		layers[0] = new Layer(v1, o1, random);
		layers[1] = new Layer(v1+o1, o2, random);
	}
	
	
	/**
	 * Returns the result of evaluating the central map on an array of field elements.
	 * 
	 * @param x the array on which the map is to be evaluated
	 * @return the result of the evaluation
	 */
	public byte[] eval(byte[] x) {
		byte[] x1, x2, res1, res2, res;
		x1 = new byte[v1+o1];
		x2 = new byte[x.length];
		
		for (int i = 0; i < v1+o1; i++) {
			x1[i] = x[i];
		}
		for (int i = 0; i < x.length; i++) {
			x2[i] = x[i];
		}
		
		res = new byte[o1+o2];
		res1 = layers[0].eval(x1);
		res2 = layers[1].eval(x2);
		
		for (int i = 0; i < o1; i++) {
			res[i] = res1[i];
		}
		for (int i = 0; i < o2; i++) {
			res[i+o1] = res2[i];
		}
		
		return res;
	}
	
	//DEBUG FUNCTION
	/*public MultQuad[] getPoly(int layer, int poly) {
		return layers[layer].poly[poly];
	}*/
	
	/**
	 * Returns the layers of this central map.
	 * @return the layers of this central map
	 */
	public Layer[] getLayers() {
		return layers;
	}
	
	/**
	 * Returns an array x such that, given input y and being F this central map then y=F(x).
	 * 
	 * @param input the result of an evaluation of this map
	 * @return an array such that when this map is evaluated on it the result is {@code input}
	 */
	public  byte[] invF(byte[] input) {
		SecureRandom random = new SecureRandom();

		int	o1=this.getLayers()[0].getOi();
		int	v1=this.getLayers()[0].getVi();
		int o2=this.getLayers()[1].getOi();
		
		byte[] ran=new byte[v1];
		byte[] res1=new byte[o1];
		byte[] res2=new byte[o2];
		byte[] res3=new byte[v1+o1];
		byte[] res=new byte[v1+o1+o2];
		byte[][] mat1=new byte[o1][o1];
		byte[] vet1=new byte[o1];
		byte[][] mat2=new byte[o2][o2];
		byte[] vet2=new byte[o2];
		boolean boo=false;
	

		do {
			//generate a random array of length o1
			for(int i=0;i<o1;i++) {
				ran[i]=(byte)random.nextInt(16);
			}
			//build a matrix and a vector by evaluating the first layer using the vi random array
			for(int i=0;i<o1;i++) {
				MultQuad[] poly =this.getLayers()[0].getPoly()[i];
				byte[] tmp1=GF16.prodVectMat(ran, poly[1].getQuad());
				vet1[i]=GF16.add(poly[0].eval(ran,ran),input[i]);
				for (int j=0;j<o1;j++) {

					mat1[i][j]=GF16.add(tmp1[j],poly[2].getLin()[j] );
				}
			}
			//try to solve the system, if it is solved, use the result to evaluate the vinegar variables 
			//of the second layer
			res1=GF16.SolveSys(mat1, vet1);
			
			if(res1!=null) {
				boo=true;
				System.arraycopy(ran, 0, res3, 0, ran.length);
				System.arraycopy(res1, 0, res3, ran.length, res1.length);
			}

			if(boo) {

				boo=false;

				for(int i=0;i<o2;i++) {
					MultQuad[] poly =this.getLayers()[1].getPoly()[i];
					byte[] tmp1=GF16.prodVectMat(res3, poly[1].getQuad());
					vet2[i]=GF16.add(poly[0].eval(res3,res3),input[o1+i]);
					for (int j=0;j<o2;j++) {

						mat2[i][j]=GF16.add(tmp1[j],poly[2].getLin()[j] );
					}
				}
				res2=GF16.SolveSys(mat2, vet2);
				if(res2!=null) {

					boo=true;
					
					System.arraycopy(res3, 0, res, 0, res3.length);
					System.arraycopy(res2, 0, res, res3.length, res2.length);

				}
			}
			
		}while(!boo);
		
		
		return res;
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

}
