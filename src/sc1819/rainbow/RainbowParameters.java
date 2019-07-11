package sc1819.rainbow;

/**
 * This class is used to easily retrieve the parameters of the Rainbow signature scheme.
 */
public class RainbowParameters {

    /**
     * Contains the parameters for the Rainbow signature scheme: v1,o1,o2
     */
    private int[] parameters;

    private String paramString;
    private String hashString;
    private String signatureString;

    public RainbowParameters() {
        this(32,32,32);
    }

    public RainbowParameters(int v1, int o1, int o2) {
        parameters = new int[]{v1, o1, o2};

        paramString = "Rainbow(16," + parameters[0] + "," + parameters[1] + "," + parameters[2] + ")";
        hashString = (parameters[0] + parameters[1]) / 2 + " bytes";
        signatureString = (parameters[0] + parameters[1] + parameters[2]) / 2 + " bytes";
    }

    /**
     * Returns the parameter v1.
     *
     * @return v1
     */
    public byte getv1() {
        return (byte) parameters[0];
    }

    /**
     * Returns the parameter o1.
     *
     * @return o1
     */
    public byte geto1() {
        return (byte) parameters[1];
    }

    /**
     * Returns the parameter o2.
     *
     * @return o2
     */
    public byte geto2() {
        return (byte) parameters[2];
    }

    public String getParamString() {
        return paramString;
    }

    public String getHashSizeString() {
        return hashString;
    }

    public String getSignatureSizeString() {
        return signatureString;
    }
}
