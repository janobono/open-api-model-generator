package sk.janobono.openapi.lib.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class RecordMetaData {
    private final String key;
    private final String name;
    private final String description;
    private final Set<String> imports;
    private final Set<String> annotations;
    private final Set<String> members;
    private final Map<String, String> enums;

    public RecordMetaData(final String key, final String name, final String description) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.imports = new HashSet<>();
        this.annotations = new HashSet<>();
        this.members = new HashSet<>();
        this.enums = new HashMap<>();
    }
}
