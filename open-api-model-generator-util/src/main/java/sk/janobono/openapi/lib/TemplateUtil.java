package sk.janobono.openapi.lib;

import sk.janobono.openapi.lib.model.RecordMetaData;
import sk.janobono.openapi.lib.model.TemplateRegex;
import sk.janobono.openapi.lib.model.TemplateResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class TemplateUtil {

    private TemplateUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String loadTemplate(final TemplateResource template) {
        try (
                final InputStream is = TemplateUtil.class.getResourceAsStream(template.resource());
                final ByteArrayOutputStream os = new ByteArrayOutputStream()
        ) {
            final byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while (bytesRead != -1) {
                bytesRead = is.read(buffer);
                if (bytesRead > 0) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            return os.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatTemplate(final String template, final String recordPackage, final RecordMetaData recordMetaData) {
        return formatTemplate(template, Map.of(
                TemplateRegex.PACKAGE, recordPackage,
                TemplateRegex.IMPORT, TemplateUtil.toStringStripTrailing(recordMetaData.getImports()),
                TemplateRegex.DESCRIPTION, recordMetaData.getDescription(),
                TemplateRegex.ANNOTATION, TemplateUtil.toString(recordMetaData.getAnnotations()),
                TemplateRegex.NAME, recordMetaData.getName(),
                TemplateRegex.MEMBERS, TemplateUtil.toStringRemoveLastChar(recordMetaData.getMembers())
        ));
    }

    public static String formatTemplate(final String template, final Map<TemplateRegex, String> replaceMap) {
        String result = template;
        for (final Map.Entry<TemplateRegex, String> entry : replaceMap.entrySet()) {
            final TemplateRegex templateRegex = entry.getKey();
            final String replacement = entry.getValue();
            result = result.replaceAll(templateRegex.getRegex(), replacement);
        }
        return result;
    }

    public static String toString(final Set<String> strings) {
        return strings.stream().sorted().reduce("", (s, s2) -> s + s2 + "\n");
    }

    public static String toStringStripTrailing(final Set<String> strings) {
        return toString(strings).stripTrailing();
    }

    public static String toStringRemoveLastChar(final Set<String> strings) {
        String result = toStringStripTrailing(strings);
        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String toCamelCase(final String string) {
        final StringBuilder sb = new StringBuilder();
        final String[] parts = string.toLowerCase().split("_");
        boolean first = true;
        for (final String part : parts) {
            if (first) {
                sb.append(part);
                first = false;
            } else {
                if (part.length() < 2) {
                    sb.append(part.toUpperCase());
                } else {
                    sb.append(Character.toString(part.charAt(0)).toUpperCase());
                    sb.append(part.substring(1));
                }
            }
        }
        return sb.toString();
    }

    public static String toSnakeCase(final String string) {
        final StringBuilder sb = new StringBuilder();
        final char c = string.charAt(0);
        sb.append(Character.toLowerCase(c));
        for (int i = 1; i < string.length(); i++) {
            final char ch = string.charAt(i);
            if (Character.isUpperCase(ch)) {
                sb.append('_');
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String toPlural(final String string) {
        if (string.endsWith("y")) {
            return string.substring(0, string.length() - 1) + "ies";
        }
        if (string.endsWith("s")) {
            return string;
        }
        return string + "s";
    }
}
