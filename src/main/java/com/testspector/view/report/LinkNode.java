package com.testspector.view.report;


import java.net.URI;

public class LinkNode extends BestPracticeViolationNode {


    private final String linkText;
    private final URI uri;

    public LinkNode(URI uri, String linkText) {
        super();
        this.uri = uri;
        this.linkText = linkText;
    }

    public String getLinkText() {
        return linkText;
    }

    public URI getUri() {
        return uri;
    }
}
