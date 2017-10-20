package eu.clarussecure.dataoperations;

import java.util.List;
import java.util.Map;

/**
 * CLARUS Data Operation module interface.
 */
public interface DataOperation {
    /** Outbound GET operation.
     * <p>{@code attributeNames} and attribute names in {@code criteria} must be fully qualified (i.e. dataset/data/attribute).
     * <br>- Attribute names may contain asterisks in the first parts (e.g. *&#47;*&#47;pat_name),
     * but not at the end (i.e. dataset/data/*) because the protection module might not support it.
     * <br>- Callers (i.e. the protocol module) should not pass more than one occurrence for each attribute name
     * because the protection module might not support it.
     * <br>- The protection modules must resolve the {@code attributeNames} and the attribute names in {@code criteria}
     * according to the security policy.
     * <br>- However, callers (i.e. the protocol module) must avoid passing attribute names that are not protected.
     * <br>- Callers can call the {@link head} operation to resolve the attribute names before calling the Outbound GET operation.
     * </p><p>
     * The Outbound GET operation returns a promise (see Inbound {@link get} operation below) that consists of a {@code List}
     * of {@code DataOperationCommands}.
     * <br>- There must be one {@code DataOperationCommands} per CSP, even for CSPs that are not involved in the GET operation.
     * <br>- {@code attributeNames}, {@code protectedAttributeNames}, {@code mapping} and {@code criteria} in {@code DataOperationCommands} must not be {@code null}.
     * <br>{@code DataOperationCommands.attributeNames} must be equal to the input {@code attributeNames} and must be in the same order.
     * <br>{@code DataOperationCommands.protectedAttributeNames} must contain the list of protected attribute names
     * managed by the CSP.
     * <br>- The list is empty if the CSP is not involved in the GET operation.
     * <br>- The protected attribute names must be in the same order than the {@code DataOperationCommands.attributeNames}
     * <br>{@code DataOperationCommands.mapping} must map the attribute names to protected attribute names managed by the CSP.
     * <br>- The mapping is empty if the CSP is not involved in the GET operation.
     * <br>- The mapping must map elements of the {@code DataOperationCommands.attributeNames} to the elements of
     * {@code DataOperationCommands.protectedAttributeNames} managed by the CSP.
     * <br>- All the elements of {@code DataOperationCommands.protectedAttributeNames} must appear in the
     * {@code DataOperationCommands.mapping}, which is not always the case for the {@code DataOperationCommands.attributeNames}.
     * <br>- However, all the elements of {@code DataOperationCommands.attributeNames} must appear in one of the {@code DataOperationCommands.mapping}, whatever the CSP is.
     * <br>{@code DataOperationCommands.criteria} must be the protected version of the input {@code criteria} for the CSP.
     * <br>- The list is empty if the CSP is not involved in the GET operation.
     * <br>- {@code DataOperationCommands.criteria} must be in the same order than the input {@code criteria}.
     * </p>
     * @param attributeNames names of the attributes, as given by the request.
     * @param criteria conditions of the get call.
     * @return a {@code List} of {@code DataOperationCommands} (the promise).
     */
    public List<DataOperationCommand> get(String[] attributeNames, Criteria[] criteria);

    /** Inbound GET operation (RESPONSE), reconstructs data received by CSP.
     * <p>Callers (i.e. the protocol module) should avoid modifying the promise.
     * <br>The protection modules must rely only on the promise and on the security policy to process the contents.
     * <br>Callers (i.e. the protocol module) must pass the {@code contents} according to the promise:
     * <br>- {@code contents} cannot be {@code null} and must have the same size than the number of {@code DataOperationCommands} (number of CSPs).
     * <br>- For each CSP, the number of rows must be equal, except if the CSP is not involved in the results (in this case, the number of rows must be zero)
     * <br>- For each CSP, the number of columns must be equal to the size of the {@code DataOperationCommands.protectedAttributeNames}.
     * <br>- In each row, values must be in the same order than the {@code DataOperationCommands.protectedAttributeNames}.
     * </p><p>
     * The Inbound GET operation returns a {@code List} of {@code DataOperationResults}.
     * Note that {@code DataOperationCommand} and {@code DataOperationResponse} might be returned.
     * <br> - Callers (i.e. the protocol module) should differentiate the two kinds of results via a {@code instanceof}
     * instruction.
     * <br>{@code DataOperationResponse} contain the reconstructed data.
     * <br>- {@code DataOperationResponse.attributeNames} must be equal to the input {@code DataOperationCommands.attributeNames} and must be in the same order.
     * <br>- {@code DataOperationResponse.contents} must have the same column size than the {@code DataOperationResponse.attributeNames} and must be in the same order.
     * <br>- {@code DataOperationResponse.contents} must have a row size lesser or equal to the row size of the input {@code contents}.
     * <br>{@code DataOperationCommand} should trigger a new call to the CSP.
     * </p>
     * @param promise references to the original call.
     * @param contents data returned by the CSP.
     * @return a {@code List} of {@code DataOperationResults}.
     */
    public List<DataOperationResult> get(List<DataOperationCommand> promise, List<String[][]> contents);

    /** Outbound POST Operation, modifies data according to security policy.
     * <p>{@code attributeNames} must be fully qualified (i.e. dataset/data/attribute).
     * <br>- {@code attributeNames} may contain asterisks in the first parts (e.g. *&#47;*&#47;pat_name),
     * but not at the end (i.e. dataset/data/*).
     * <br>- Each attribute name should not appear more than once.
     * <br>- The protection modules must resolve the {@code attributeNames} according to the security policy.
     * <br>- Callers (i.e. the protocol module) must avoid passing attribute names that are not protected.
     * <br>- Callers can call the {@link head} operation to resolve the attribute names before calling the Outbound POST operation.
     * <br>{@code contents} must be coherent with {@code attributeNames}:
     * <br>- {@code contents} cannot be {@code null} and the number of columns must be equal to the size of {@code attributeNames}:
     * <br>- {@code contents} values must be in the same order than the {@code attributeNames}.
     * <br>- {@code contents} may contain zero rows.
     * </p><p>
     * The Outbound POST operation returns a result that consists of a {@code List} of {@code DataOperationCommand}.
     * <br>- There must be one {@code DataOperationCommands} per CSP, even for CSPs that are not involved in the POST operation.
     * <br>- {@code attributeNames}, {@code protectedAttributeNames}, {@code mapping} and {@code protectedContents} in {@code DataOperationCommands} must not be {@code null}.
     * <br>{@code DataOperationCommands.attributeNames} must be equal to the input {@code attributeNames} and must be in the same order.
     * <br>{@code DataOperationCommands.protectedAttributeNames} must contain the list of protected attribute names
     * managed by the CSP.
     * <br>- The list is empty if the CSP is not involved in the POST operation.
     * <br>- The protected attribute names must be in the same order than the {@code DataOperationCommands.attributeNames}
     * <br>{@code DataOperationCommands.mapping} must map the attribute names to protected attribute names managed by the CSP.
     * <br>- The mapping is empty if the CSP is not involved in the POST operation.
     * <br>- The mapping must map elements of the {@code DataOperationCommands.attributeNames} to the elements of
     * {@code DataOperationCommands.protectedAttributeNames} managed by the CSP.
     * <br>- All the elements of {@code DataOperationCommands.protectedAttributeNames} must appear in the
     * {@code DataOperationCommands.mapping}, which is not always the case for the {@code DataOperationCommands.attributeNames}.
     * <br>- However, all the elements of {@code DataOperationCommands.attributeNames} must appear in one of the {@code DataOperationCommands.mapping}, whatever the CSP is.
     * <br>{@code DataOperationCommands.protectedContents} contain the protected data for protected attributes managed by the CSP.
     * <br>- {@code DataOperationCommands.protectedContents} must have the same column size than the {@code DataOperationCommands.protectedAttributeNames}.
     * <br>- Values in the {@code DataOperationCommands.protectedContents} must be in the same order than the {@code DataOperationCommands.protectedAttributeNames}.
     * <br>- The number of rows in {@code DataOperationCommands.protectedContents} must be lesser or equal to the row size of the input {@code contents}.
     * <br>- However, the number of rows in {@code DataOperationCommands.protectedContents} must be equal for all the CSPs, except if CSP is not involved in the POST operation (in this case the number of rows must be zero).
     * </p>
     * @param attributeNames names of the attributes, as given by the request.
     * @param contents unprotected records
     * @return a {@code List} of {@code DataOperationCommand}
     */
    public List<DataOperationCommand> post(String[] attributeNames, String[][] contents);

    /** Outbound PUT Operation, modifies data specified by criteria, according to security policy.
     * <p>{@code attributeNames} and attribute names in {@code criteria} must be fully qualified (i.e. dataset/data/attribute).
     * <br>- Attribute names may contain asterisks in the first parts (e.g. *&#47;*&#47;pat_name),
     * but not at the end (i.e. dataset/data/*) because the protection module might not support it.
     * <br>- Callers (i.e. the protocol module) should not pass more than one occurrence for each attribute name
     * because the protection module might not support it.
     * <br>- The protection modules must resolve the {@code attributeNames} and the attribute names in {@code criteria}
     * according to the security policy.
     * <br>- However, callers (i.e. the protocol module) must avoid passing attribute names that are not protected.
     * <br>- Callers can call the {@link head} operation to resolve the attribute names before calling the Outbound PUT operation.
     * <br>{@code contents} must be coherent with {@code attributeNames}:
     * <br>- {@code contents} cannot be {@code null} and the number of columns must be equal to the size of {@code attributeNames}:
     * <br>- {@code contents} values must be in the same order than the {@code attributeNames}.
     * <br>- {@code contents} may contain zero rows.
     * </p><p>
     * The Outbound PUT operation returns a result that consists of a {@code List} of {@code DataOperationCommand}.
     * <br>- There must be one {@code DataOperationCommands} per CSP, even for CSPs that are not involved in the PUT operation.
     * <br>- {@code attributeNames}, {@code protectedAttributeNames}, {@code mapping}, {@code criteria} and {@code protectedContents} in {@code DataOperationCommands} must not be {@code null}.
     * <br>{@code DataOperationCommands.attributeNames} must be equal to the input {@code attributeNames} and must be in the same order.
     * <br>{@code DataOperationCommands.protectedAttributeNames} must contain the list of protected attribute names
     * managed by the CSP.
     * <br>- The list is empty if the CSP is not involved in the PUT operation.
     * <br>- The protected attribute names must be in the same order than the {@code DataOperationCommands.attributeNames}
     * <br>{@code DataOperationCommands.mapping} must map the attribute names to protected attribute names managed by the CSP.
     * <br>- The mapping is empty if the CSP is not involved in the PUT operation.
     * <br>- The mapping must map elements of the {@code DataOperationCommands.attributeNames} to the elements of
     * {@code DataOperationCommands.protectedAttributeNames} managed by the CSP.
     * <br>- All the elements of {@code DataOperationCommands.protectedAttributeNames} must appear in the
     * {@code DataOperationCommands.mapping}, which is not always the case for the {@code DataOperationCommands.attributeNames}.
     * <br>- However, all the elements of {@code DataOperationCommands.attributeNames} must appear in one of the {@code DataOperationCommands.mapping}, whatever the CSP is.
     * <br>{@code DataOperationCommands.criteria} must be the protected version of the input {@code criteria} for the CSP.
     * <br>- The list is empty if the CSP is not involved in the PUT operation.
     * <br>- {@code DataOperationCommands.criteria} must be in the same order than the input {@code criteria}.
     * <br>{@code DataOperationCommands.protectedContents} contain the protected data for protected attributes managed by the CSP.
     * <br>- {@code DataOperationCommands.protectedContents} must have the same column size than the {@code DataOperationCommands.protectedAttributeNames}.
     * <br>- Values in the {@code DataOperationCommands.protectedContents} must be in the same order than the {@code DataOperationCommands.protectedAttributeNames}.
     * <br>- The number of rows in {@code DataOperationCommands.protectedContents} must be lesser or equal to the row size of the input {@code contents}.
     * <br>- However, the number of rows in {@code DataOperationCommands.protectedContents} must be equal for all the CSPs, except if CSP is not involved in the PUT operation (in this case the number of rows must be zero).
     * </p>
     * @param attributeNames names of the attributes, as given by the request.
     * @param criteria conditions of the get call, in the same order as the attributeNames.
     * @param contents unprotected records
     * @return a List of DataOperationCommand object, which contains the necessary information
     *         to build a call to the CSP.
     */
    public List<DataOperationCommand> put(String[] attributeNames, Criteria[] criteria, String[][] contents);

    /** Outbound DELETE Operation, deletes data specified by criteria.
     * <p>{@code attributeNames} and attribute names in {@code criteria} must be fully qualified (i.e. dataset/data/attribute).
     * <br>- Attribute names may contain asterisks in the first parts (e.g. *&#47;*&#47;pat_name),
     * but not at the end (i.e. dataset/data/*) because the protection module might not support it.
     * <br>- Callers (i.e. the protocol module) should not pass more than one occurrence for each attribute name
     * because the protection module might not support it.
     * <br>- The protection modules must resolve the {@code attributeNames} and the attribute names in {@code criteria}
     * according to the security policy.
     * <br>- However, callers (i.e. the protocol module) must avoid passing attribute names that are not protected.
     * <br>- Callers can call the {@link head} operation to resolve the attribute names before calling the Outbound DELETE operation.
     * </p><p>
     * The Outbound DELETE operation returns a result that consists of a {@code List} of {@code DataOperationCommand}.
     * <br>- There must be one {@code DataOperationCommands} per CSP, even for CSPs that are not involved in the DELETE operation.
     * <br>- {@code attributeNames}, {@code protectedAttributeNames}, {@code mapping} and {@code criteria} in {@code DataOperationCommands} must not be {@code null}.
     * <br>{@code DataOperationCommands.attributeNames} must be equal to the input {@code attributeNames} and must be in the same order.
     * <br>{@code DataOperationCommands.protectedAttributeNames} must contain the list of protected attribute names
     * managed by the CSP.
     * <br>- The list is empty if the CSP is not involved in the DELETE operation.
     * <br>- The protected attribute names must be in the same order than the {@code DataOperationCommands.attributeNames}
     * <br>{@code DataOperationCommands.mapping} must map the attribute names to protected attribute names managed by the CSP.
     * <br>- The mapping is empty if the CSP is not involved in the DELETE operation.
     * <br>- The mapping must map elements of the {@code DataOperationCommands.attributeNames} to the elements of
     * {@code DataOperationCommands.protectedAttributeNames} managed by the CSP.
     * <br>- All the elements of {@code DataOperationCommands.protectedAttributeNames} must appear in the
     * {@code DataOperationCommands.mapping}, which is not always the case for the {@code DataOperationCommands.attributeNames}.
     * <br>- However, all the elements of {@code DataOperationCommands.attributeNames} must appear in one of the {@code DataOperationCommands.mapping}, whatever the CSP is.
     * <br>{@code DataOperationCommands.criteria} must be the protected version of the input {@code criteria} for the CSP.
     * <br>- The list is empty if the CSP is not involved in the DELETE operation.
     * <br>- {@code DataOperationCommands.criteria} must be in the same order than the input {@code criteria}.
     * </p>
     * @param attributeNames names of the attributes, as given by the request.
     * @param criteria conditions of the get call, in the same order as the attributeNames.
     */
    public List<DataOperationCommand> delete(String[] attributeNames, Criteria[] criteria);

    /** Returns the mappings between unprotected attribute names and protected attribute
     * names.
     * <p>{@code attributeNames} must be fully qualified (i.e. dataset/data/attribute).
     * <br>- {@code attributeNames} may contain asterisks in any part (e.g. *&#47;*&#47;pat_name or dataset/data/*).
     * <br>- Each attribute name can appear more than once.
     * <br>- The protection modules must resolve the {@code attributeNames} according to the security policy.
     * <br>- For attribute names that end with an asterisk (*), the protection modules must resolve it according to the security policy
     * and replace it by all the attributes which are defined in the security policy and that match the attribute name pattern.
     * <br>- Callers (i.e. the protocol module) can pass any attribute name.
     * <br>- The protection module must ignore attribute names that are unknown (not defined in the security policy).
     * </p><p>
     * The HEAD operation returns a {@code List} of {@code Map}.
     * <br>- There must be one {@code Map} per CSP, even for CSPs that are not involved.
     * <br>- Each {@code Map} must not be {@code null}.
     * <br>- Each {@code Map} must map the attribute names to protected attribute names managed by the CSP.
     * <br>- A {@code Map} is empty if the CSP is not involved.
     * <br>- Attribute names that are ignored (not defined in the security policy) are not mapped.
     * <br>- For each attribute name that ends with an asterisks (*), one or more {@code Map} contain one
     * or more mapping of the matching attribute names that are defined in the security policy.
     * </p>
     * @param attributeNames unprotected attribute names.
     * @return a List of Mapping objects.
     */
    public List<Map<String, String>> head(String[] attributeNames);
}
