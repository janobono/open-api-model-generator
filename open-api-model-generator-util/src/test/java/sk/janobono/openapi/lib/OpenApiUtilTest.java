package sk.janobono.openapi.lib;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import sk.janobono.openapi.lib.model.RecordMetaData;
import sk.janobono.openapi.lib.model.RecordParserParameters;
import sk.janobono.openapi.lib.model.TemplateRegex;
import sk.janobono.openapi.lib.model.TemplateResource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenApiUtilTest {

    @Test
    void openApiUtilTest() {
        final OpenAPI openAPI = new OpenAPIV3Parser().read(this.getClass().getResource("/api-docs.json").getPath());

        final RecordParserParameters parameters = RecordParserParameters.builder()
                .recordFilterRegex("^(?!.*PageableObject|.*PageProduct|.*Pageable|.*SortObject).*$")
                .recordNameRegex("[^a-zA-Z0-9]")
                .recordNameSuffix("")
                .fieldNameRegex("[^a-zA-Z0-9]")
                .enumNameFilterRegex("\\b[A-Z0-9`_]+\\b")
                .commonEnumNames(Map.of("ProductDataStatus", "ProductStatus", "ProductDataCurrency", "ProductCurrency"))
                .defaultDataType("String")
                .build();

        final List<RecordMetaData> records = new RecordParser(openAPI, parameters).getRecords();
        final Map<String, String> enums = new HashMap<>();

        assertThat(records).isNotNull();
        assertThat(records.size()).isEqualTo(2);

        final String enumTemplate = TemplateUtil.loadTemplate(TemplateResource.ENUM);
        final String recordTemplate = TemplateUtil.loadTemplate(TemplateResource.RECORD);

        records.forEach(recordMetaData -> {
            enums.putAll(recordMetaData.getEnums());

            if (recordMetaData.getMembers().size() > 2) {
                recordMetaData.getImports().add("import lombok.Builder;");
                recordMetaData.getAnnotations().add("@Builder");
            }

            final String result = TemplateUtil.formatTemplate(recordTemplate, "sk.janobono.openapi.test", recordMetaData);
            final String expected;
            try {
                expected = Files.readString(Paths.get(getClass().getClassLoader().getResource(recordMetaData.getName() + ".txt").toURI()));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            assertThat(result).isEqualTo(expected);
        });

        assertThat(enums.size()).isEqualTo(2);

        enums.forEach((enumName, enumValue) -> {
            final String result = TemplateUtil.formatTemplate(enumTemplate, Map.of(
                    TemplateRegex.PACKAGE, "sk.janobono.openapi.test",
                    TemplateRegex.NAME, enumName,
                    TemplateRegex.MEMBERS, enumValue
            ));
            final String expected;
            try {
                expected = Files.readString(Paths.get(getClass().getClassLoader().getResource(enumName + ".txt").toURI()));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            assertThat(result).isEqualTo(expected);
        });
    }
}
