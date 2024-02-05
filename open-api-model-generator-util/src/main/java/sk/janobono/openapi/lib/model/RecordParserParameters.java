package sk.janobono.openapi.lib.model;

import lombok.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Builder
public record RecordParserParameters(
        String recordFilterRegex,
        String recordNameRegex,
        String recordNameSuffix,
        String fieldFilterRegex,
        String fieldNameRegex,
        String enumNameFilterRegex,
        Map<String, String> commonEnumNames,
        String defaultDataType
) {
}
