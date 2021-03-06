package eu.clarussecure.dataoperations;

import java.io.Serializable;

/**
 * Parent class in the hierarchy of DataOperation
 * result objects. DataOperationCommand and
 * DataOperationResponse both extend this class.
 * Created by URV.
 */
public abstract class DataOperationResult implements Serializable {

    /**
     * Unique identifier of the object within
     * the CLARUS proxy. Meant for bookkeeping.
     */
    protected int id;

    /**
     * Attribute names in the call that originated this
     * object. Common in both Command and Response objects.
     */
    protected String[] attributeNames;

    public int getId() {
        return id;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(String[] attributeNames) {
        this.attributeNames = attributeNames;
    }
}
