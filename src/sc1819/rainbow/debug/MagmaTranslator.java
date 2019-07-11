package sc1819.rainbow.debug;

import sc1819.rainbow.util.CentralMap;
import sc1819.rainbow.util.GF16;
import sc1819.rainbow.util.Layer;
import sc1819.rainbow.util.MultQuad;

import java.security.SecureRandom;
import java.util.Arrays;

public class MagmaTranslator {

    public MagmaTranslator() {

    }

    public void centralMapEval(CentralMap F, byte[] x) {
        //Prova eval con traduzione per magma
        Layer layer;
        MultQuad[] poly;
        MultQuad p;
        int vi, oi;

        int v1 = F.getLayers()[0].getVi();
        int o1 = F.getLayers()[0].getOi();
        int o2 = F.getLayers()[1].getOi();

        System.out.println("***MAGMA INPUT***");
        System.out.println("clear;");
        System.out.println("F<a>:=GF(16);");
        System.out.println("R<[x]>:=PolynomialRing(F," + (o1 + o2 + v1) + ");");

        for (int i = 0; i < 2; i++) {
            layer = F.getLayers()[i];
            vi = layer.getVi();
            oi = layer.getOi();

            for (int j = 0; j < layer.getPoly().length; j++) {
                System.out.print("f" + i + "" + j + " := ");
                poly = layer.getPoly()[j];

                p = poly[0];
                for (int r = 0; r < p.getQuad().length; r++) {
                    for (int c = 0; c < p.getQuad()[0].length; c++) {
                        if (r <= c && p.getQuad()[r][c] != 0)
                            System.out.print("a^" + GF16.logsTable[p.getQuad()[r][c]] + "*x[" + (r + 1) + "]*x[" + (c + 1) + "]+");
                    }
                }

                p = poly[1];
                for (int r = 0; r < p.getQuad().length; r++) {
                    for (int c = 0; c < p.getQuad()[0].length; c++) {
                        if (p.getQuad()[r][c] != 0)
                            System.out.print("a^" + GF16.logsTable[p.getQuad()[r][c]] + "*x[" + (r + 1) + "]*x[" + (c + vi + 1) + "]+");
                    }
                }

                p = poly[0];
                for (int r = 0; r < p.getLin().length; r++) {
                    if (p.getLin()[r] != 0)
                        System.out.print("a^" + GF16.logsTable[p.getLin()[r]] + "*x[" + (r + 1) + "]+");
                }

                p = poly[2];
                for (int r = 0; r < p.getLin().length; r++) {
                    if (p.getLin()[r] != 0)
                        System.out.print("a^" + GF16.logsTable[p.getLin()[r]] + "*x[" + (r + vi + 1) + "]+");
                }

                p = poly[0];
                if (p.getTerm() != 0) System.out.println("a^" + GF16.logsTable[p.getTerm()] + ";");
                else System.out.println("0;");
            }
        }
        System.out.print("f := [");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < F.getLayers()[i].getPoly().length; j++) {
                if (i != 1 || j != (F.getLayers()[1].getPoly().length - 1)) System.out.print("f" + i + "" + j + ", ");
            }
        }
        System.out.println("f1" + (F.getLayers()[1].getPoly().length - 1 + "];"));
        System.out.print("[Evaluate(f[i], [");
        for (int i = 0; i < x.length - 1; i++) {
            if (x[i] != 0) System.out.print("a^" + GF16.logsTable[x[i]] + ", ");
            else System.out.print("0, ");
        }
        if (x[x.length - 1] != 0)
            System.out.println("a^" + GF16.logsTable[x[x.length - 1]] + "]) : i in [1.." + (o1 + o2) + "]];");
        else System.out.println("0]) : i in [1.." + (o1 + o2) + "]];");

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
        MagmaTranslator d = new MagmaTranslator();
        int size = 3;
        byte[] x = new byte[size];
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < size; i++) {
            x[i] = (byte) random.nextInt(16);
        }
        d.centralMapEval(new CentralMap(1, 1, 1, new SecureRandom()), x);

    }

}
