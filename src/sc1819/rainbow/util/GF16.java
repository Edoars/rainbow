package sc1819.rainbow.util;

/**
 * This class provides the basic operations like addition, multiplication and
 * finding the multiplicative inverse of an element in GF16.
 * <p>
 * The operations are implemented using the irreducible polynomial
 * x^4+x+1.
 * <p>
 * This class makes use of lookup tables(exps and logs) for implementing the
 * operations in order to keep computations efficient.
 * <p>
 * This class includes some methods to compute matrix and vector
 * multiplications inside of the field.
 */

public class GF16 {
    //GF(16) = F2[x]/(x^4+x+1)
    //a3*x^3+a2*x^2+a1*x+a0 -> (a3,a2,a1,a0) binario
    //                                       0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
    /**
     * this lookup table is needed for multiplication and computing the
     * multiplicative inverse
     */
    public static final byte[] expsTable = {1, 2, 4, 8, 3, 6, 12, 11, 5, 10, 7, 14, 15, 13, 9, 1};
    /**
     * this lookup table is needed for multiplication and computing the
     * multiplicative inverse
     */
    public static final byte[] logsTable = {-1, 0, 1, 4, 2, 8, 5, 10, 3, 14, 9, 7, 6, 13, 11, 12};

    /**
     * Returns the sum of two field elements, computed casting into byte.
     *
     * @param x the first element of GF16 to be added
     * @param y the second element of GF16 to be added
     * @return the sum of x and y in GF16
     */
    static public byte add(byte x, byte y) {
        return (byte) (x ^ y);
    }

    /**
     * Product of field elements, computed using the log and exp tables.
     * The cases x=0 or y=0 are handled separately to return 0.
     *
     * @param x the first element of GF16 to be multiplied
     * @param y the second element of GF16 to be multiplied
     * @return the product of x and y in GF16
     */
    static public byte mult(byte x, byte y) {
        if (x == 0 || y == 0) {
            return 0;
        } else {
            return expsTable[(logsTable[x] + logsTable[y]) % 15];
        }
    }

    /**
     * Returns the multiplicative inverse of a field element different from zero in said field.
     * This is computed using the log and exp tables. The case x=0 is handled separately to return zero.
     *
     * @param x the GF16 element of which we want to produce the inverse
     * @return the multiplicative inverse of x in GF16, or zero if x=0
     */
    static public byte inv(byte x) {
        if (x == 0) {
            return 0;
        }

        return expsTable[15 - logsTable[x]];
    }

    /**
     * Computes the inverse of a matrix, both with coefficients in GF16.
     * This is obtained through Gaussian-elimination, operating on a matrix A to get A=[id|inv] with inv = mat^(-1).
     *
     * @param mat the matrix with coefficients in GF16 of which the inverse is to be calculated
     * @return the inverse matrix of mat in GF16
     */
    static public byte[][] matrixInverse(byte[][] mat) {
        byte[][] inv;
        byte[][] A = new byte[mat.length][2 * mat.length];

        //Copy mat in a=[mat|id]
        for (int i = 0; i < mat.length; i++) {
            System.arraycopy(mat[i], 0, A[i], 0, mat.length);

            for (int j = mat.length; j < (2 * mat.length); j++) {
                A[i][j] = 0;
            }

            A[i][i + mat.length] = 1;
        }

        //Apply Gaussian-elimnation to obtain a=[id|inv] with inv = mat^(-1)
        byte c, t;
        //Apply rows operations to to the left matrix to obtain an upper triangular matrix
        //R_i -> R_i + a[i][j]/a[j][j]*R_j
        for (int i = 1; i < mat.length; i++) {
            for (int j = 0; j < i; j++) {
                c = GF16.inv(A[j][j]);
                if (c == 0) {
                    return null;
                }
                c = GF16.mult(c, A[i][j]); //A[i][j]/A[j][j]

                for (int k = j; k < (2 * mat.length); k++) {
                    t = GF16.mult(c, A[j][k]);
                    A[i][k] = GF16.add(A[i][k], t);
                }
            }
        }

        //Multiply each row by A[i][i] to get 1 on the diagonal
        //R_i -> R_i / a[i][i]
        for (int i = 0; i < mat.length; i++) {
            c = GF16.inv(A[i][i]);
            if (c == 0) {
                return null;
            }

            for (int k = 0; k < (2 * mat.length); k++) {
                A[i][k] = GF16.mult(A[i][k], c);
            }
        }

        //Apply rows operation to the left matrix to obtain the identity matrix
        for (int i = mat.length - 2; i >= 0; i--) {
            for (int j = mat.length - 1; j > i; j--) {
                c = A[i][j];

                for (int k = j; k < (2 * mat.length); k++) {
                    t = GF16.mult(c, A[j][k]);
                    A[i][k] = GF16.add(A[i][k], t);
                }
            }
        }

        inv = new byte[mat.length][mat.length];
        for (int i = 0; i < mat.length; i++) {
            System.arraycopy(A[i], mat.length, inv[i], 0, mat.length);
        }

        return inv;
    }

    /**
     * Solves a linear system in GF16 of the form (mat)*x=vec through Gaussian-elimination.
     *
     * @param mat the matrix part of the system (coefficients in GF16)
     * @param vec the vector part of the system (elements of GF16)
     * @return the solution to the linear system in GF16
     */
    public static byte[] SolveSys(byte[][] mat, byte[] vec) {
        int n = mat.length;
        int m = mat[0].length;
        if (n != m) {
            throw new IllegalArgumentException("Matrici quadrate pls");
        }
        byte[] res = new byte[n];
        //gaussian elimination
        byte[][] A = new byte[n][n + 1];

        //Copy mat in a=[mat|b]
        for (int i = 0; i < n; i++) {
            System.arraycopy(mat[i], 0, A[i], 0, n);
            A[i][n] = vec[i];
        }

        //Apply Gaussian-elimnation to obtain a=[id|b]
        byte c, t;
        //Apply rows operations to to the left matrix to obtain an upper triangular matrix
        //R_i -> R_i + a[i][j]/a[j][j]*R_j
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                c = GF16.inv(A[j][j]);
                if (c == 0) {
                    return null;
                }
                c = GF16.mult(c, A[i][j]); //A[i][j]/A[j][j]

                for (int k = j; k < n + 1; k++) {
                    t = GF16.mult(c, A[j][k]);
                    A[i][k] = GF16.add(A[i][k], t);
                }
            }
        }
        //solve the upper triangular system

        if (A[n - 1][n - 1] != 0) res[n - 1] = GF16.mult(GF16.inv(A[n - 1][n - 1]), A[n - 1][n]);
        else return null;
        for (int k = n - 2; k >= 0; k--) {
            byte tmp = 0;
            for (int i = k + 1; i < n; i++) {
                tmp = GF16.add(tmp, GF16.mult(A[k][i], res[i]));
            }

            res[k] = GF16.mult(GF16.add(A[k][n], tmp), GF16.inv(A[k][k]));
        }
        return res;

    }

    /**
     * Computes the product of a vector with a matrix in GF16.
     *
     * @param vec the vector of GF16 elements that is to be multiplied
     * @param mat the matrix with GF16 coefficients that is to be multiplied
     * @return the product (vec)*(mat)
     */
    static public byte[] prodVectMat(byte[] vec, byte[][] mat) {
        if (vec.length != mat.length) {
            throw new IllegalArgumentException("Matrici di dim diversa");
        }

        byte[] res = new byte[mat[0].length];

        for (int i = 0; i < mat[0].length; i++) {
            res[i] = 0;

            for (int j = 0; j < mat.length; j++) {
                res[i] = GF16.add(res[i], GF16.mult(mat[j][i], vec[j]));
            }
        }

        return res;
    }

    /**
     * Computes the product of a matrix with a vector in GF16.
     *
     * @param mat the matrix with GF16 coefficients that is to be multiplied
     * @param vec the vector of GF16 elements that is to be multiplied
     * @return the product (mat)*(vec)
     */
    static public byte[] prodMatVec(byte[][] mat, byte[] vec) {
        if (vec.length != mat[0].length) {
            throw new IllegalArgumentException("Matrici di dim diversa");
        }

        byte[] res = new byte[mat.length];

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                res[i] = GF16.add(res[i], GF16.mult(mat[i][j], vec[j]));
            }
        }

        return res;
    }

    /**
     * Computes the product two vectors in GF16.
     *
     * @param vec1 the first vector of GF16 elements that is to be multiplied
     * @param vec2 the second vector of GF16 elements that is to be multiplied
     * @return the product (vec1)*(vec2)^T
     */
    static public byte prodVecVec(byte[] vec1, byte[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vettori di dim diversa");
        }

        byte res = 0;

        for (int i = 0; i < vec1.length; i++) {
            res = GF16.add(res, GF16.mult(vec1[i], vec2[i]));
        }

        return res;
    }

    /**
     * Compute the sums of two vectors in GF16.
     *
     * @param vec1 the first vector of GF16 elements
     * @param vec2 the second vector of GF16 elements
     * @return the sum (vec1)+(vec2)
     */
    static public byte[] addVectors(byte[] vec1, byte[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vettori di dim diversa");
        }

        byte[] res = new byte[vec1.length];

        for (int i = 0; i < vec1.length; i++) {
            res[i] = GF16.add(vec1[i], vec2[i]);
        }

        return res;
    }

    static public String toHex(byte[] data) {
        String digits = "0123456789abcdef";

        StringBuilder buf = new StringBuilder();

        for (byte datum : data) {
            buf.append(digits.charAt(datum & 0x0f));
        }

        return buf.toString();
    }

    public static void main(String[] args) {
        byte x = 0, y = 8; //(x+1)*x^3 = x^3+x+1 =11
        System.out.println("x+y = " + GF16.add(x, y));
        System.out.println("x*y = " + GF16.mult(x, y));
        System.out.println("x^-1 = " + GF16.inv(x));
        System.out.println("y^-1 = " + GF16.inv(y));
    }
}

