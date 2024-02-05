package sk.janobono.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import sk.janobono.openapi.lib.RecordParser;
import sk.janobono.openapi.lib.TemplateUtil;
import sk.janobono.openapi.lib.model.RecordMetaData;
import sk.janobono.openapi.lib.model.RecordParserParameters;
import sk.janobono.openapi.lib.model.TemplateRegex;
import sk.janobono.openapi.lib.model.TemplateResource;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mojo(name = "gen")
public class OpenApiPluginMojo extends AbstractMojo {

    @Parameter(required = true)
    private File sourceFile;

    @Parameter(required = true, defaultValue = "${basedir}/target/generated-sources/open-api")
    private File targetDir;

    @Parameter(required = true)
    private String packageName;

    @Parameter(required = true, defaultValue = "[^a-zA-Z0-9]")
    String nameRegex;

    @Parameter
    String recordFilter;

    @Parameter
    String fieldFilter;

    @Parameter
    String recordNameSuffix;

    @Parameter(required = true, defaultValue = "\\b[A-Z0-9`_]+\\b")
    String enumNameFilter;

    @Parameter
    Map<String, String> commonEnumNames;

    @Parameter(required = true, defaultValue = "String")
    String defaultDataType;

    @Parameter(required = true, defaultValue = "2")
    private int builderFieldsCount;

    @Override
    public void execute() {
        final OpenAPI openAPI = new OpenAPIV3Parser().read(sourceFile.getAbsolutePath());

        final RecordParserParameters parameters = RecordParserParameters.builder()
                .recordFilterRegex(recordFilter)
                .recordNameRegex(nameRegex)
                .recordNameSuffix(recordNameSuffix == null ? "" : recordNameSuffix)
                .fieldFilterRegex(fieldFilter)
                .fieldNameRegex(nameRegex)
                .enumNameFilterRegex(enumNameFilter)
                .commonEnumNames(commonEnumNames)
                .defaultDataType(defaultDataType)
                .build();

        final File packageDir = getPackageDir(targetDir, packageName);

        if (packageDir.exists() || packageDir.mkdirs()) {
            write(openAPI, parameters, packageDir, packageName, builderFieldsCount);
        } else {
            getLog().warn("Unable to create client directory.");
        }
    }

    private File getPackageDir(final File parentDir, final String packageName) {
        final File result;
        if (Objects.isNull(packageName) || packageName.isBlank()) {
            result = parentDir;
        } else {
            result = new File(parentDir, File.separator + packageName.replace("\\.", File.separator));
        }
        return result;
    }

    private void write(final OpenAPI openAPI, final RecordParserParameters parameters, final File packageDir, final String packageName, final int builderFieldsCount) {
        final String enumTemplate = TemplateUtil.loadTemplate(TemplateResource.ENUM);
        final String recordTemplate = TemplateUtil.loadTemplate(TemplateResource.RECORD);

        final List<RecordMetaData> records = new RecordParser(openAPI, parameters).getRecords();
        final Map<String, String> enums = new HashMap<>();

        records.forEach(recordMetaData -> {
            enums.putAll(recordMetaData.getEnums());

            if (recordMetaData.getMembers().size() > builderFieldsCount) {
                recordMetaData.getImports().add("import lombok.Builder;");
                recordMetaData.getAnnotations().add("@Builder");
            }

            final String content = TemplateUtil.formatTemplate(recordTemplate, packageName, recordMetaData);
            writeContent(new File(packageDir, recordMetaData.getName() + ".java"), content);
        });

        enums.forEach((enumName, enumValue) -> {
            final String content = TemplateUtil.formatTemplate(enumTemplate, Map.of(
                    TemplateRegex.PACKAGE, packageName,
                    TemplateRegex.NAME, enumName,
                    TemplateRegex.MEMBERS, enumValue
            ));
            writeContent(new File(packageDir, enumName + ".java"), content);
        });
    }

    private void writeContent(final File file, final String content) {
        final byte[] data = content.getBytes();
        try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(file, false))) {
            os.write(data, 0, data.length);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
