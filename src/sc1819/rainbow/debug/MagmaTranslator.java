package sc1819.rainbow.debug;

import sc1819.rainbow.RainbowKeyPair;
import sc1819.rainbow.RainbowPubKey;
import sc1819.rainbow.RainbowScheme;
import sc1819.rainbow.RainbowSecKey;
import sc1819.rainbow.util.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MagmaTranslator {

    private RainbowSecKey secKey;
    private RainbowPubKey pubKey;
    private int m, n;

    public MagmaTranslator(RainbowKeyPair keyPair) {
        this.secKey = keyPair.getSk();
        this.pubKey = keyPair.getPk();

        this.m = secKey.getEqNum();
        this.n = secKey.getVarNum();
    }

    public String setup() {
        StringBuilder sb = new StringBuilder();

        sb.append("clear;");
        sb.append(System.lineSeparator());

        sb.append("F<a>:=GF(16);");
        sb.append(System.lineSeparator());

        sb.append("R<[x]>:=PolynomialRing(F," + n + ");");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String translateKeyGeneration() {
        StringBuilder sb = new StringBuilder();

        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Setup");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append(this.setup());
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Private key");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append("// Affine map S");
        sb.append(System.lineSeparator());
        sb.append(this.translateAffineMap(secKey.getS(), "s"));
        sb.append(System.lineSeparator());

        sb.append("// Central map F");
        sb.append(System.lineSeparator());
        sb.append(this.translateCentralMap(secKey.getF(), "f"));
        sb.append(System.lineSeparator());

        sb.append("// Affine map T");
        sb.append(System.lineSeparator());
        sb.append(this.translateAffineMap(secKey.getT(), "t"));
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Public key");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append(this.translatePublicKey(pubKey, "g"));
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Verify that s(f(t)) = g");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append("f_comp_t := &cat[[Evaluate(f[i], t) : i in [1..2]],[0,0]];");
        sb.append("f_comp_t := &cat[[Evaluate(f[i], t) : i in [1.." + this.m + "]],");
        sb.append("[0 : i in [1.." + (this.n - this.m) + "]]];");
        sb.append(System.lineSeparator());
        sb.append("s_comp_f_comp_t := [Evaluate(s[i], f_comp_t) : i in [1.." + this.m + "]];");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("assert s_comp_f_comp_t eq g;");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String translateVerify(String filePath, String signaturePath) {
        StringBuilder sb = new StringBuilder();

        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Setup");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append(this.setup());
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Public key");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append(this.translatePublicKey(pubKey, "g"));
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Hashed document");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        byte[] digest = RainbowScheme.hashFile(filePath, this.m);
        sb.append("hash := [");
        for (byte elem : digest) {
            sb.append(byteToField(elem));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("];");
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Signature");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        byte[] signature = RainbowScheme.loadSignature(signaturePath);
        sb.append("sign := [");
        for (byte elem : signature) {
            sb.append(byteToField(elem));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("];");
        sb.append(System.lineSeparator());


        sb.append("//");
        sb.append(System.lineSeparator());
        sb.append("// Verify that g(signature) = hash");
        sb.append(System.lineSeparator());
        sb.append("//");
        sb.append(System.lineSeparator());

        sb.append("sign_evaluated := [Evaluate(g[i], sign) : i in [1.." + this.m + "]];");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("assert sign_evaluated eq hash;");

        return sb.toString();
    }

    public String translateAffineMapToMatrix(AffineMap map, String name) {
        StringBuilder sb = new StringBuilder();

        byte[][] matrix = map.getMatrix();
        byte[] vector = map.getVector();

        sb.append(name.toUpperCase());
        sb.append(":=Matrix(F," + matrix.length + "," + matrix[0].length + ",[");
        for (byte[] row : matrix) {
            sb.append("[");
            for (byte elem : row) {
                sb.append(byteToField(elem));
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("],");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]);");
        sb.append(System.lineSeparator());

        sb.append("v" + name.toLowerCase());
        sb.append(":=Matrix(F," + vector.length + ",1,[");
        for (byte elem : vector) {
            sb.append(byteToField(elem));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]);");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String translateAffineMap(AffineMap map, String name) {
        StringBuilder sb = new StringBuilder();

        byte[][] matrix = map.getMatrix();
        byte[] vector = map.getVector();

        String polyName;
        ArrayList<String> polyColletion = new ArrayList<>();

        for (int i = 0; i < matrix.length; i++) {
            polyName = name.toLowerCase() + i;
            polyColletion.add(polyName);

            sb.append(polyName);
            sb.append(":=");
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(byteToField(matrix[i][j]) + "*x[" + (j + 1) + "]+");
            }
            sb.append(byteToField(vector[i]));
            sb.append(";");

            sb.append(System.lineSeparator());
        }

        sb.append(name.toLowerCase());
        sb.append(":=[");
        for (String poly : polyColletion) {
            sb.append(poly);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("];");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String translateCentralMap(CentralMap F, String name) {
        StringBuilder sb = new StringBuilder();

        int vi, oi;
        int layerIndex = 0, polyIndex;

        String polyName;
        ArrayList<String> polyCollection = new ArrayList<>();

        for (Layer layer : F.getLayers()) {
            vi = layer.getVi();
            oi = layer.getOi();

            polyIndex = 0;

            for (MultQuad[] poly : layer.getPoly()) {
                polyName = name.toLowerCase() + layerIndex + "" + polyIndex;
                polyCollection.add(polyName);

                sb.append(translatePoly(poly, vi, polyName));

                polyIndex++;
            }

            layerIndex++;
        }

        sb.append(name.toLowerCase());
        sb.append(":=[");
        for (String poly : polyCollection) {
            sb.append(poly);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("];");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String translatePublicKey(RainbowPubKey pubKey, String name) {
        StringBuilder sb = new StringBuilder();

        int polyIndex = 0;

        String polyName;
        ArrayList<String> polyColletion = new ArrayList<>();

        for (MultQuad poly : pubKey.getPoly()) {
            polyName = name.toLowerCase() + polyIndex;
            polyColletion.add(polyName);

            sb.append(translateMultQuad(poly, polyName));

            polyIndex++;
        }

        sb.append(name.toLowerCase());
        sb.append(":=[");
        for (String poly : polyColletion) {
            sb.append(poly);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("];");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    private String translateMultQuad(MultQuad poly, String name) {
        StringBuilder sb = new StringBuilder();

        byte elem;

        byte[][] quad = poly.getQuad();
        byte[] lin = poly.getLin();
        byte term = poly.getTerm();

        sb.append(name);
        sb.append(":=");

        for (int i = 0; i < quad.length; i++) {
            for (int j = 0; j < quad[0].length; j++) {
                elem = quad[i][j];

                if (elem != 0)
                    sb.append(byteToField(elem) + "*x[" + (i + 1) + "]*x[" + (j + 1) + "]+");
            }
        }

        for (int i = 0; i < lin.length; i++) {
            elem = lin[i];

            if (elem != 0)
                sb.append(byteToField(elem) + "*x[" + (i + 1) + "]+");
        }

        sb.append(byteToField(term));
        sb.append(";");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    private String translatePoly(MultQuad[] poly, int vi, String name) {
        StringBuilder sb = new StringBuilder();

        MultQuad component;

        sb.append(name);
        sb.append(":=");

        component = poly[0];
        for (int i = 0; i < component.getQuad().length; i++) {
            for (int j = 0; j < component.getQuad()[0].length; j++) {
                if (i <= j && component.getQuad()[i][j] != 0)
                    sb.append(byteToField(component.getQuad()[i][j]) + "*x[" + (i + 1) + "]*x[" + (j + 1) + "]+");
            }
        }

        component = poly[1];
        for (int i = 0; i < component.getQuad().length; i++) {
            for (int j = 0; j < component.getQuad()[0].length; j++) {
                if (component.getQuad()[i][j] != 0)
                    sb.append(byteToField(component.getQuad()[i][j]) + "*x[" + (i + 1) + "]*x[" + (j + vi + 1) + "]+");
            }
        }

        component = poly[0];
        for (int i = 0; i < component.getLin().length; i++) {
            if (component.getLin()[i] != 0)
                sb.append(byteToField(component.getLin()[i]) + "*x[" + (i + 1) + "]+");
        }

        component = poly[2];
        for (int i = 0; i < component.getLin().length; i++) {
            if (component.getLin()[i] != 0)
                sb.append(byteToField(component.getLin()[i]) + "*x[" + (i + vi + 1) + "]+");
        }

        component = poly[0];
        if (component.getTerm() != 0) sb.append(byteToField(component.getTerm()) + ";");
        else sb.append("0;");

        sb.append(System.lineSeparator());

        return sb.toString();
    }

    private String byteToField(byte elem) {
        if (elem == 0) {
            return "0";
        }

        return "a^" + GF16.logsTable[elem];
    }

    public static void main(String[] args) throws FileNotFoundException {
        /*MagmaTranslator d = new MagmaTranslator();
        int size = 4;
        byte[] x = new byte[size];
        SecureRandom random = new FixedRand();
        for (int i = 0; i < size; i++) {
            x[i] = (byte) random.nextInt(16);
        }

        RainbowSecKey sk = RainbowSecKey.loadKey("sk.txt");
        d.centralMapEval(sk.getF(), x);*/

        /*RainbowKeyPair keyPair = new RainbowKeyPair("pk.txt", "sk.txt");
        MagmaTranslator translator = new MagmaTranslator(keyPair);

        System.out.println(translator.setup());

        RainbowSecKey secKey = keyPair.getSk();

        System.out.println(translator.translateAffineMap(secKey.getS(), "s"));
        System.out.println(translator.translateCentralMap(secKey.getF(),"f"));
        System.out.println(translator.translateAffineMap(secKey.getT(), "t"));

        RainbowPubKey pubKey = keyPair.getPk();
        System.out.println(translator.translatePublicKey(pubKey, "g"));*/

        /*byte[] x = new byte[]{2,3,2,4};
        byte[] res = T.eval(x);
        System.out.println("x: " + Arrays.toString(x));
        System.out.println("F(x): " + Arrays.toString(res));
        System.out.print("Log: [");
        for (int i = 0; i < res.length - 1; i++) {
            System.out.print(GF16.logsTable[res[i]] + ", ");
        }
        System.out.println(GF16.logsTable[res[res.length - 1]] + "]");*/

        RainbowKeyPair keyPair = new RainbowKeyPair("pk.txt", "sk.txt");
        MagmaTranslator translator = new MagmaTranslator(keyPair);

        System.out.println(translator.translateVerify("test.txt", "test.sig"));
        /*PrintStream ps = new PrintStream("out.txt");
        System.setOut(ps);

        System.out.println(translator.translateKeyGeneration());*/


    }

}
