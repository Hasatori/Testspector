package com.testspector.model.enums;


import com.thaiopensource.util.Uri;
import org.apache.http.client.utils.URIBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.testspector.model.utils.Constants.WEB_PAGE_BEST_PRACTICES_ULR;

public enum BestPractice {


    TEST_ONLY_PUBLIC_BEHAVIOUR("Test only public behaviour", "","defining-tests/code-coverage#testonlythepublicbehaviourofthetestedsystem"),
    AT_LEAST_ONE_ASSERTION("At least one assertion", "","creating-tests/assertions#atleastoneassertionpertest"),
    ONLY_ONE_ASSERTION("Only one assertion", "","creating-tests/assertions#onlyoneassertionpertest"),
    NO_GLOBAL_STATIC_PROPERTIES("No global static properties", "","creating-tests/independence#donotuseglobalstaticproperties"),
    SETUP_A_TEST_NAMING_STRATEGY("Setup a test naming strategy", "","creating-tests/naming-conventions#setupatestnamingstrategy"),
    CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS("Catch tested exceptions using framework or library tools", "","creating-tests/testing-exceptions#catchtestedexceptionsusingframeworkorlibrarytools"),
    NO_CONDITIONAL_LOGIC("No conditional logic", "","creating-tests/conditional-logic#donotuseif,switch,fororwhileblocksinatest");

    static {
        ONLY_ONE_ASSERTION.relatedRules = Collections.singletonList(AT_LEAST_ONE_ASSERTION);
        AT_LEAST_ONE_ASSERTION.relatedRules = Collections.singletonList(ONLY_ONE_ASSERTION);
    }

    private final String displayName;
    private final String definition;
    private  URI webPageHyperlink;
    private List<BestPractice> relatedRules = Collections.emptyList();


    BestPractice(String displayName, String definition,String webPageSection) {
        this.displayName = displayName;
        this.definition = definition;
        try {
            this.webPageHyperlink = new URI(String.format("%s/%s",WEB_PAGE_BEST_PRACTICES_ULR,webPageSection));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefinition() {
        return definition;
    }

    public List<BestPractice> getRelatedRules() {
        return relatedRules;
    }

    public URI getWebPageHyperlink() {
        return webPageHyperlink;
    }
}
