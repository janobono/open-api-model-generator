package sk.janobono.openapi.lib.model;

public enum TemplateRegex {
    PACKAGE("<REPLACE_PACKAGE>"),
    IMPORT("<REPLACE_IMPORT>"),
    DESCRIPTION("<REPLACE_DESCRIPTION>"),
    ANNOTATION("<REPLACE_ANNOTATION>"),
    NAME("<REPLACE_NAME>"),
    MEMBERS("<REPLACE_MEMBERS>");

    private final String regex;

    TemplateRegex(final String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
