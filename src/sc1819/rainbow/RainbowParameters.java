package sc1819.rainbow;

/**
 * This class is used to easily retrieve the parameters of the Rainbow signature scheme.
 */
class RainbowParameters {

    /**
     * Contains the parameters for the Rainbow signature scheme: v1,o1,o2
     */
    private final byte[] parameters = {1,1,1}; //{32,32,32};

    public static final String PARAM_STRING = "Rainbow(16,32,32,32)";
    public static final String HASH_SIZE = "32 bytes";
    public static final String SIGNATURE_SIZE = "48 bytes";

    /**
     * Returns an array containing all the parameters of the scheme.
     *
     * @return the array of parameters
     */
    public byte[] getParameters() {
        return parameters;
    }

    /**
     * Returns the parameter v1.
     *
     * @return v1
     */
    public byte getv1() {
        return parameters[0];
    }

    /**
     * Returns the parameter o1.
     *
     * @return o1
     */
    public byte geto1() {
        return parameters[1];
    }

    /**
     * Returns the parameter o2.
     *
     * @return o2
     */
    public byte geto2() {
        return parameters[2];
    }
}
