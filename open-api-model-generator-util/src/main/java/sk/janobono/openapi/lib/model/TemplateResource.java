package sk.janobono.openapi.lib.model;

public enum TemplateResource {
    ENUM("/enum.txt"),
    RECORD("/record.txt");

    private final String resource;

    TemplateResource(final String resource) {
        this.resource = resource;
    }

    public String resource() {
        return resource;
    }
}
