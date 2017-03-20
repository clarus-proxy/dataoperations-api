package eu.clarussecure.dataoperations;

import java.io.InputStream;

/**
 * Parent class for data objects returned by several
 * methods in the API. Commands contain information
 * that the protocol module needs to build calls directed
 * at the CSP.
 * Created by URV.
 */
public abstract class DataOperationCommand extends DataOperationResult {

    /**
     * Names of the protected attributes
     */
    protected String[] protectedAttributeNames;

    /**
     * Names of the binary attributes contained in
     * the InputStream extraBinaryContent.
     */
    protected String[] extraProtectedAttributeNames;

    /**
     * Binary data to append to the call. The
     * internal structure and parsing of this
     * data is the responsibility of the module
     * developer.
     */
    protected InputStream extraBinaryContent;

    /**
     * Mapping between the original attribute names
     * and the protected attribute names.
     */
    protected Mapping mapping;

    /**
     * Protected content. Formatted the same
     * way as the original contents.
     */
    protected String[][] protectedContents;

    /**
     * Search criteria
     */
    protected Criteria[] criteria;


    public String[] getProtectedAttributeNames() {
        return protectedAttributeNames;
    }

    public String[] getExtraProtectedAttributeNames() {
        return extraProtectedAttributeNames;
    }

    public InputStream getExtraBinaryContent() {
        return extraBinaryContent;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public Criteria[] getCriteria() {
        return criteria;
    }

    public String[][] getProtectedContents() {
        return protectedContents;
    }
}
