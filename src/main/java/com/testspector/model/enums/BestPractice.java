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


    TEST_ONLY_PUBLIC_BEHAVIOUR("Test only public behaviour", "","testovat-pouze-verejne-chovani-testovaneho-systemu"),
    AT_LEAST_ONE_ASSERTION("At least one assertion", "","minimalne-jedna-overovaci-metoda-na-test"),
    ONLY_ONE_ASSERTION("Only one assertion", "","prave-jedna-overovaci-metoda-na-test"),
    NO_GLOBAL_STATIC_PROPERTIES("No global static properties", "","nepouzivat-globalni-staticke-promenne"),
    SETUP_A_TEST_NAMING_STRATEGY("Setup a test naming strategy", "","urcit-strategii-pojmenovani-testu"),
    CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS("Catch exceptions using frameworks tools", "","odchytavat-testovane-vyjimky-pomoci-nastroju-knihoven-ci-testovacich-frameworku"),
    NO_CONDITIONAL_LOGIC("No conditional logic", "","podminena-logika"),
    NO_DEAD_CODE("No dead code", "","odstranovat-mrtvy-kod");

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
            this.webPageHyperlink = new URI(String.format("%s#%s",WEB_PAGE_BEST_PRACTICES_ULR,webPageSection));
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
