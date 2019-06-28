package sc1819.rainbow.util;

import java.security.SecureRandom;
import java.util.Arrays;

public class Debug {

    public Debug() {

    }

    public void centralMapEval(CentralMap F, int n, byte[] x) {
        //Prova eval con traduzione per magma
        Layer layer;
        MultQuad[] poly;
        MultQuad p;
        int vi, oi;

        System.out.println("***MAGMA INPUT***");
        System.out.println("clear;");
        System.out.println("F<a>:=GF(16);");
        System.out.println("R<[x]>:=PolynomialRing(F," + (F.o1 + F.o2 + F.v1) + ");");

        for (int i = 0; i < 2; i++) {
            layer = F.layers[i];
            vi = layer.vi;
            oi = layer.oi;

            for (int j = 0; j < layer.poly.length; j++) {
                System.out.print("f" + i + "" + j + " := ");
                poly = layer.poly[j];

                p = poly[0];
                for (int r = 0; r < p.quad.length; r++) {
                    for (int c = 0; c < p.quad[0].length; c++) {
                        if (r <= c && p.quad[r][c] != 0)
                            System.out.print("a^" + GF16.logsTable[p.quad[r][c]] + "*x[" + (r + 1) + "]*x[" + (c + 1) + "]+");
                    }
                }

                p = poly[1];
                for (int r = 0; r < p.quad.length; r++) {
                    for (int c = 0; c < p.quad[0].length; c++) {
                        if (p.quad[r][c] != 0)
                            System.out.print("a^" + GF16.logsTable[p.quad[r][c]] + "*x[" + (r + 1) + "]*x[" + (c + vi + 1) + "]+");
                    }
                }

                p = poly[0];
                for (int r = 0; r < p.lin.length; r++) {
                    if (p.lin[r] != 0) System.out.print("a^" + GF16.logsTable[p.lin[r]] + "*x[" + (r + 1) + "]+");
                }

                p = poly[2];
                for (int r = 0; r < p.lin.length; r++) {
                    if (p.lin[r] != 0) System.out.print("a^" + GF16.logsTable[p.lin[r]] + "*x[" + (r + vi + 1) + "]+");
                }

                p = poly[0];
                if (p.term != 0) System.out.println("a^" + GF16.logsTable[p.term] + ";");
                else System.out.println("0;");
            }
        }
        System.out.print("f := [");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < F.layers[i].poly.length; j++) {
                if (i != 1 || j != (F.layers[1].poly.length - 1)) System.out.print("f" + i + "" + j + ", ");
            }
        }
        System.out.println("f1" + (F.layers[1].poly.length - 1 + "];"));
        System.out.print("[Evaluate(f[i], [");
        for (int i = 0; i < x.length - 1; i++) {
            if (x[i] != 0) System.out.print("a^" + GF16.logsTable[x[i]] + ", ");
            else System.out.print("0, ");
        }
        if (x[x.length - 1] != 0)
            System.out.println("a^" + GF16.logsTable[x[x.length - 1]] + "]) : i in [1.." + (F.o1 + F.o2) + "]];");
        else System.out.println("0]) : i in [1.." + (F.o1 + F.o2) + "]];");

        System.out.println("***INPUT END***\n");

        byte[] res = F.eval(x);
        System.out.println("x: " + Arrays.toString(x));
        System.out.println("F(x): " + Arrays.toString(res));
        System.out.print("Log: [");
        for (int i = 0; i < res.length - 1; i++) {
            System.out.print(GF16.logsTable[res[i]] + ", ");
        }
        System.out.println(GF16.logsTable[res[res.length - 1]] + "]");
    }

    public static void main(String[] args) {
        Debug d = new Debug();
        byte[] x = new byte[30];
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 30; i++) {
            x[i] = (byte) random.nextInt(16);
        }
        d.centralMapEval(new CentralMap(10, 10, 10, new SecureRandom()), 30, x);

    }

}
