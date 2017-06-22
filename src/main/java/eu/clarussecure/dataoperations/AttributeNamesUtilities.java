package eu.clarussecure.dataoperations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributeNamesUtilities {

    private static final Pattern START_WITH_DOUBLE_ASTERISKS = Pattern.compile("^([^/*]*\\*/[^/*]*\\*/)([^/*]*)");
    private static final Pattern START_WITH_SINGLE_ASTERISK = Pattern.compile("^([^/*]*\\*/)([^/*]*/[^/*]*)");

    /**
     * Replace unqualified attribute names by a generic qualified one (with
     * asterisks): <blockquote>
     *
     * <pre>
     *
     * attribute1 -> *&#47*&#47attribute1
     * data/attribute2 -> *&#47data/attribute2
     * dataset/data/attribute3 -> dataset/data/attribute3
     *
     * </pre>
     *
     * </blockquote>
     * <p>
     *
     * @param attributeNames
     *            the (potentially unqualified) attribute names
     * @return the fully qualified attribute names
     */
    public static List<String> fullyQualified(List<String> attributeNames) {
        return attributeNames.stream()
                .map(an -> an.indexOf('/') == -1
                        // prepend with */*/ if there is no /
                        ? "*/*/" + an
                        : an.indexOf('/') == an.lastIndexOf('/')
                                // prepend with */ if there is one /
                                ? "*/" + an
                                // do nothing if there is two /
                                : an)
                .collect(Collectors.toList());
    }

    /**
     * Resolves the request attribute names that contain asterisk (*) according
     * to the attributes the protection module has to protect (i.e. the
     * attributes which are defined in the security policy).
     * <p>
     * The request attribute names and the attributes to protect the must be
     * fully qualified (dataset/data/attribute). Any part of an attribute name
     * can contain an asterisk (i.e. dataset, data and/or attribute can be *)
     * <p>
     * For example, with the following attributes to protect: <blockquote>
     * *&#47;patient/pat_id, *&#47;patient/pat_name, *&#47;patient/pat_last1,
     * *&#47;patient/pat_last2 </blockquote>
     * <p>
     * The request attributes <code>*&#47;patient/*</code> is resolved to:
     * <blockquote>
     *
     * *&#47;patient/pat_id, *&#47;patient/pat_name, *&#47;patient/pat_last1,
     * *&#47;patient/pat_last2
     *
     * </blockquote>
     * <p>
     * The request attributes
     * <code>*&#47;patient/pat_name, *&#47;patient/*, *&#47;patient/pat_id</code>
     * is resolved to: <blockquote>
     *
     * *&#47;patient/pat_name, *&#47;patient/pat_id, *&#47;patient/pat_name,
     * *&#47;patient/pat_last1, *&#47;patient/pat_last2, *&#47;patient/pat_id
     *
     * </blockquote>
     * <p>
     *
     * @param requestAttributeNames
     *            the request attribute names to resolve.
     * @param attributesToProtect
     *            the attribute the protection module protect (i.e. the
     *            attributes which are defined in the security policy)
     * @return the resolved request attribute names
     */
    public static String[] resolve(String[] requestAttributeNames, List<String> attributesToProtect) {
        String[] resolvedAttributeNames;
        if (Arrays.stream(requestAttributeNames).filter(an -> an.indexOf('*') != -1).count() == 0) {
            // Attribute names don't contain asterisk (*)
            resolvedAttributeNames = requestAttributeNames;
        } else {
            // Replace attribute names that contain asterisk (*) by the matching
            // data identifiers
            List<Map.Entry<String, Pattern>> attributeNamePatterns = Arrays.stream(requestAttributeNames)
                    .map(an -> new SimpleEntry<>(an, Pattern.compile(escapeRegex(an)))).collect(Collectors.toList());
            Stream<String> retainedDataIds = attributesToProtect.stream().filter(id -> id.indexOf('*') == -1);
            Stream<String> missingDataIds1 = attributesToProtect.stream()
                    .map(id -> START_WITH_DOUBLE_ASTERISKS.matcher(id)).filter(m -> m.matches())
                    .map(m -> new String[] { m.group(1), m.group(2) }).flatMap(groups -> {
                        Pattern firstPartPattern = Pattern.compile(escapeRegex(groups[0]));
                        String lastPart = groups[1];
                        return Arrays.stream(requestAttributeNames).map(an -> an.substring(0, an.lastIndexOf('/') + 1))
                                .filter(an -> firstPartPattern.matcher(an).matches()).map(an -> an + lastPart);
                    });
            Stream<String> missingDataIds2 = attributesToProtect.stream()
                    .map(id -> START_WITH_SINGLE_ASTERISK.matcher(id)).filter(m -> m.matches())
                    .map(m -> new String[] { m.group(1), m.group(2) }).flatMap(groups -> {
                        Pattern firstPartPattern = Pattern.compile(escapeRegex(groups[0]));
                        String lastPart = groups[1];
                        return Arrays.stream(requestAttributeNames).map(an -> an.substring(0, an.indexOf('/') + 1))
                                .filter(an -> firstPartPattern.matcher(an).matches()).map(an -> an + lastPart);
                    });
            List<String> dataIds = Stream.concat(retainedDataIds, Stream.concat(missingDataIds1, missingDataIds2))
                    .distinct().collect(Collectors.toList());
            List<Map.Entry<String, Stream<String>>> resolvedDataIds = attributeNamePatterns.stream()
                    .map(e -> new SimpleEntry<>(e.getKey(),
                            dataIds.stream().filter(id -> e.getValue().matcher(id).matches())))
                    .collect(Collectors.toList());
            List<Map.Entry<String, Stream<String>>> unresolvedAttributeNames = attributeNamePatterns.stream()
                    .filter(e -> dataIds.stream().noneMatch(id -> e.getValue().matcher(id).matches()))
                    .map(e -> new SimpleEntry<>(e.getKey(), Stream.of(e.getKey()))).collect(Collectors.toList());
            // Concatenate all found attributes
            resolvedAttributeNames = Stream.concat(resolvedDataIds.stream(), unresolvedAttributeNames.stream())
                    .flatMap(Map.Entry::getValue).toArray(String[]::new);
        }
        return resolvedAttributeNames;
    }

    /**
     * Escape special characters in order to use the input String as a regular
     * expression
     *
     * @param str
     *            the string to escape special characters
     * @return a regular expression with escaped special characters
     */
    public static String escapeRegex(String str) {
        return str.replace(".", "\\.").replace("[", "\\[").replace("]", "\\]").replace("(", "\\(").replace(")", "\\)")
                .replace("*", "[^/]*");
    }

}
