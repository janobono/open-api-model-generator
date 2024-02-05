package sk.janobono.openapi.lib;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import sk.janobono.openapi.lib.model.RecordMetaData;
import sk.janobono.openapi.lib.model.RecordParserParameters;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class RecordParser {
    private final OpenAPI openAPI;
    private final RecordParserParameters parameters;
    private final Map<String, RecordMetaData> data;

    public RecordParser(final OpenAPI openAPI, final RecordParserParameters parameters) {
        this.openAPI = openAPI;
        this.parameters = parameters;
        this.data = new HashMap<>();
    }

    public List<RecordMetaData> getRecords() {
        data.clear();

        openAPI.getComponents().getSchemas().forEach((key, schema) -> {
            boolean isModelAccepted = isObject(schema) || isAllOf(schema);

            if (parameters.recordFilterRegex() != null && !parameters.recordFilterRegex().isBlank()) {
                isModelAccepted &= Pattern.matches(parameters.recordFilterRegex(), key);
            }

            if (isModelAccepted) {
                data.put(key, toRecordMetaData(key));
                return;
            }
            log.warn("Component schema not used - " + key);
        });

        return data.values().stream()
                .map(this::schemaToRecord)
                .toList();
    }

    private boolean isObject(final Schema<?> schema) {
        return schema != null
                && "object".equals(schema.getType()) && schema.getProperties() != null && !schema.getProperties().isEmpty();
    }

    private boolean isAllOf(final Schema<?> schema) {
        return schema != null
                && schema.getAllOf() != null && !schema.getAllOf().isEmpty();
    }

    private boolean isRef(final Schema<?> schema) {
        return schema != null
                && schema.get$ref() != null
                && openAPI.getComponents().getSchemas().containsKey(refToKey(schema.get$ref()));
    }

    protected boolean isFieldName(final String string) {
        if (Objects.isNull(string) || string.isBlank()) {
            return false;
        }

        if (parameters.fieldFilterRegex() != null && !parameters.fieldFilterRegex().isBlank()) {
            return Pattern.matches(parameters.fieldFilterRegex(), string);
        }

        return true;
    }

    private RecordMetaData toRecordMetaData(final String key) {
        final String name = key.replaceAll(parameters.recordNameRegex(), "")
                + parameters.recordNameSuffix();

        final String description = openAPI.getComponents().getSchemas().get(key).getDescription() != null
                ? openAPI.getComponents().getSchemas().get(key).getDescription()
                : "";

        return new RecordMetaData(key, name, description);
    }

    private RecordMetaData schemaToRecord(final RecordMetaData recordMetaData) {
        final Schema<?> schema = openAPI.getComponents().getSchemas().get(recordMetaData.getKey());
        schemaToRecord(schema, recordMetaData);
        return recordMetaData;
    }

    private void schemaToRecord(final Schema<?> schema, final RecordMetaData recordMetaData) {
        if (isObject(schema)) {
            schemaPropertiesToFields(schema, recordMetaData);
        }

        if (isAllOf(schema)) {
            schema.getAllOf().forEach(subSchema -> schemaToRecord(subSchema, recordMetaData));
        }

        if (isRef(schema)) {
            schemaToRecord(openAPI.getComponents().getSchemas().get(refToKey(schema.get$ref())), recordMetaData);
        }
    }

    private void schemaPropertiesToFields(final Schema<?> schema, final RecordMetaData recordMetaData) {
        if (schema.getProperties() != null) {
            schema.getProperties().forEach((fieldName, propertySchema) -> {
                if (isFieldName(fieldName)) {
                    schemaToField(propertySchema, fieldName, recordMetaData);
                } else {
                    log.warn(recordMetaData.getName() + " - Field " + fieldName + " not used.");
                }
            });
        }
    }

    private void schemaToField(final Schema<?> schema, final String fieldName, final RecordMetaData recordMetaData) {
        if ("string".equals(schema.getType())) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                final String normalizedFieldName = toFieldName(fieldName);
                String enumTypeName = recordMetaData.getKey()
                        + Character.toString(normalizedFieldName.charAt(0)).toUpperCase()
                        + normalizedFieldName.substring(1);
                if (parameters.commonEnumNames() != null && parameters.commonEnumNames().containsKey(enumTypeName)) {
                    enumTypeName = parameters.commonEnumNames().get(enumTypeName);
                }
                recordMetaData.getEnums().put(enumTypeName, toEnums((List<String>) schema.getEnum()));
                recordMetaData.getMembers().add(toField(fieldName, enumTypeName, fieldName));
                return;
            }

            if ("date-time".equals(schema.getFormat())) {
                recordMetaData.getImports().add("import java.time.ZonedDateTime;");
                recordMetaData.getMembers().add(toField(fieldName, "ZonedDateTime", fieldName));
                return;
            }

            recordMetaData.getMembers().add(toField(fieldName, "String", fieldName));
            return;
        }

        if ("integer".equals(schema.getType())) {
            if ("int64".equals(schema.getFormat())) {
                recordMetaData.getMembers().add(toField(fieldName, "Long", fieldName));
                return;
            }
            recordMetaData.getMembers().add(toField(fieldName, "Integer", fieldName));
            return;
        }

        if ("boolean".equals(schema.getType())) {
            recordMetaData.getMembers().add(toField(fieldName, "Boolean", fieldName));
            return;
        }

        if ("number".equals(schema.getType())) {
            recordMetaData.getImports().add("import java.math.BigDecimal;");
            recordMetaData.getMembers().add(toField(fieldName, "BigDecimal", fieldName));
            return;
        }

        if ("array".equals(schema.getType())) {
            final String dataType = getDataType(fieldName, schema);
            recordMetaData.getImports().add("import java.util.List;");
            final String propName = fieldName.contains("_") ? TemplateUtil.toCamelCase(fieldName) : fieldName;
            recordMetaData.getMembers()
                    .add(toField(fieldName, MessageFormat.format("List<{0}>", dataType), TemplateUtil.toPlural(propName)));
            return;
        }

        final String dataType = getDataType(fieldName, schema);
        recordMetaData.getMembers().add(toField(fieldName, dataType, fieldName));
    }

    private String getDataType(final String fieldName, final Schema<?> schema) {
        if (schema.getItems() != null && schema.getItems().get$ref() != null && !schema.getItems().get$ref().isEmpty()) {
            final Schema<?> items = schema.getItems();
            final String dataTypeName = refToKey(items.get$ref()) + parameters.recordNameSuffix();
            if (data.values().stream().map(RecordMetaData::getName).anyMatch(name -> name.equals(dataTypeName))) {
                return dataTypeName;
            }
        }

        if (schema.get$ref() != null) {
            final String dataTypeName = refToKey(schema.get$ref()) + parameters.recordNameSuffix();
            if (data.values().stream().map(RecordMetaData::getName).anyMatch(name -> name.equals(dataTypeName))) {
                return dataTypeName;
            }
        }

        log.warn("Default data type [" + parameters.defaultDataType() + "] used for - " + fieldName);
        return parameters.defaultDataType();
    }

    private String toField(final String openApiName, final String dataType, final String name) {
        return MessageFormat.format("        @JsonProperty(\"{0}\") {1} {2},", openApiName, dataType, toFieldName(name));
    }

    private String toFieldName(final String name) {
        final String camelCaseName = name.contains("_") ? TemplateUtil.toCamelCase(name) : name;
        return camelCaseName.replaceAll(parameters.fieldNameRegex(), "");
    }

    private String toEnums(final List<String> enums) {
        final AtomicInteger index = new AtomicInteger();
        return TemplateUtil.toStringRemoveLastChar(
                enums.stream()
                        .map(value -> value.replaceAll("\\$", "\\\\\\$"))
                        .map(value -> MessageFormat.format("    {0}(\"{1}\"),", toEnumName(value, index.getAndIncrement()), value))
                        .collect(Collectors.toSet())
        );
    }

    private String toEnumName(final String enumValue, final int index) {
        if (Pattern.matches(parameters.enumNameFilterRegex(), enumValue.strip())) {
            return enumValue.strip();
        }
        final String enumName = enumValue.replaceAll(parameters.fieldNameRegex(), "");
        if (enumName.isBlank()) {
            return "VALUE_" + index;
        }
        return TemplateUtil.toSnakeCase(enumName).toUpperCase();
    }

    private String refToKey(final String ref) {
        return ref.replace("#/components/schemas/", "");
    }
}
