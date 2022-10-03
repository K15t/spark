package com.k15t.spark.confluence;

import com.k15t.spark.base.util.StreamUtil;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SparkTestUtils {

    /**
     * Extracts property names that could be used in a Velocity template from the public methods (including inherited ones)
     * of the class using the patterns get... or is...
     *
     * @param clazz {@link Class} from which to extract the get... and is... methods
     * @param prefix prefix to add to all properties, eg. getProp -> prefix.prop and prefix.Prop
     * @return a set of strings that could be used as property names in a Velocity template when
     * an object of the class is (a part of) the context
     */
    public static Set<String> extractPossibleVelocityKeys(Class<?> clazz, String prefix) {

        HashSet<String> res = new HashSet<>();

        // getMethods() returns only public methods, including inherited ones (no further checks needed)
        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();
            String rest = null;
            if (methodName.startsWith("get")) {
                rest = methodName.substring("get".length());

            } else if (methodName.startsWith("is")) {
                rest = methodName.substring("is".length());
            }
            if (rest != null) {
                if ("".equals(rest)) {
                    throw new IllegalArgumentException("Method does not handle classes using get(\"property\"");
                } else {
                    res.add(prefix + "." + rest.substring(0, 1).toLowerCase() + rest.substring(1));
                    res.add(prefix + "." + rest.substring(0, 1).toUpperCase() + rest.substring(1));
                }
            }
        }

        return res;
    }


    /**
     * Extracts the variable references from velocity template fragment ($variable or ${variable}) and
     * counts how many times each of the variable names in the expRefs list is present in the fragment.
     *
     * @param velocityFragment fragment from which to extract variables
     * @param expRefs list of variable names to count (that are expected to be used in the fragment)
     * @param failOnUnExpectedRef if true, calls Assert.fail() when encountering a variable name not in the 'expRefs' list
     * @return map from variable name to int, from variable names in 'expRefs' list to time used in the fragment
     */
    public static Map<String, Integer> checkVelocityFragmentReferences(
            String velocityFragment, Collection<String> expRefs, boolean failOnUnExpectedRef) {

        Map<String, Integer> refCount = new HashMap<>();
        for (String expRef : expRefs) {
            refCount.put(expRef, 0);
        }

        // this should really work like a parser to be able to exclude variables defined in the
        // templates from those that need to be in the context
        // now those kind of variables need to be added in the expRefs

        // in principle variables could probably contain also at least
        // numbers and '_', update when needed
        Pattern velocityLongKeyPattern = Pattern.compile("\\$\\{([a-zA-Z.]+)\\}");
        Matcher velocityLongKeys = velocityLongKeyPattern.matcher(velocityFragment);

        Pattern velocityShortKeyPattern = Pattern.compile("\\$([a-zA-Z.]+)");
        Matcher velocityShortKeys = velocityShortKeyPattern.matcher(velocityFragment);

        List<String> foundRefs = new LinkedList<>();

        while (velocityLongKeys.find()) {
            foundRefs.add(velocityLongKeys.group(1));
        }
        while (velocityShortKeys.find()) {
            foundRefs.add(velocityShortKeys.group(1));
        }

        for (String refKey : foundRefs) {

            Integer currCount = refCount.get(refKey);

            if (currCount == null) { // unexpected refKey
                if (failOnUnExpectedRef) {
                    throw new AssertionError("Found velocity variable referencing key <" + refKey + "> that was not " +
                            "in the list of expected keys (" + expRefs + ")");
                }
            } else {
                refCount.put(refKey, currCount + 1);
            }

        }

        return refCount;
    }


    /**
     * Test that the properties used in the template file in given path come from the actionClass
     * ({@link #extractPossibleVelocityKeys(Class, String) extractPossibleVelocityKeys()} is used with "action" as prefix)
     * or from the list of extraProps known to be available (eg. properties defined inside the template).
     *
     * @param actionClass (action) class to test
     * @param templatePath path from which to load the template
     * @param extraProps list of extra property names known to be available in the context where the template will be used
     * @throws Exception eg. if the template is not found
     */
    public static void testActionClassHasTemplateProps(
            Class<?> actionClass, String templatePath, String... extraProps) throws Exception {

        try (InputStream templateStream = SparkTestUtils.class.getClassLoader().getResourceAsStream(templatePath)) {
            String template = StreamUtil.toString(templateStream);

            Set<String> possVariables = extractPossibleVelocityKeys(actionClass, "action");

            Collections.addAll(possVariables, extraProps);

            checkVelocityFragmentReferences(template, possVariables, true);
        }

    }

}
