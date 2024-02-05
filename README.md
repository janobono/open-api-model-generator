# open-api-model-generator

Open API Initiative model generator maven plugin. Based
on [swagger-parser](https://github.com/swagger-api/swagger-parser).

## Main parameters

| Property name      | Required | Default                                      | Description                                                              |
|--------------------|----------|----------------------------------------------|--------------------------------------------------------------------------|
| sourceFile         | Y        |                                              | Open api documentation file in JSON or YAML format.                      |
| targetDir          | Y        | ${basedir}/target/generated-sources/open-api | Target directory.                                                        |
| packageName        | Y        |                                              | Model package name.                                                      |
| nameRegex          | Y        | [^a-zA-Z0-9]                                 | Regular expression used to remove invalid name characters.               |
| recordFilter       | N        |                                              | Regular expression used to filter record names.                          |
| fieldFilter        | N        |                                              | Regular expression used to filter field names.                           |
| recordNameSuffix   | N        |                                              | Record name suffix.                                                      |
| enumNameFilter     | Y        | \\b[A-Z0-9`_]+\\b                            | Regular expression used to check if enum value can be used as enum name. |
| commonEnumNames    | N        |                                              | Map of enum replaced enum names.                                         |
| defaultDataType    | Y        | String                                       | Default field data type if correct one not detected.                     |
| builderFieldsCount | Y        | 2                                            | When number of fields is bigger than @Builder will be added.             |

### Example project

- [Example project pom](./open-api-model-generator-example/pom.xml)
- [Open API json](./open-api-model-generator-example/src/open-api/open_api_generator_example.json)
- [Generated sources test](./open-api-model-generator-example/src/test/java/sk/janobono/example/api/controller/ProductControllerTest.java)

```shell
mvn clean generate-sources
```

```xml

<plugin>
    <groupId>sk.janobono</groupId>
    <artifactId>open-api-model-generator-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>base config</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>gen</goal>
            </goals>
            <configuration>
                <sourceFile>${basedir}/src/open-api/open_api_generator_example.json</sourceFile>
                <packageName>sk.janobono.example.client.model.base</packageName>
            </configuration>
        </execution>
        <execution>
            <id>full config</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>gen</goal>
            </goals>
            <configuration>
                <sourceFile>${basedir}/src/open-api/open_api_generator_example.json</sourceFile>
                <targetDir>${basedir}/target/generated-sources/open-api</targetDir>
                <packageName>sk.janobono.example.client.model.full</packageName>
                <nameRegex>[^a-zA-Z0-9]</nameRegex>
                <recordFilter>^(?!.*PageableObject|.*PageProduct|.*Pageable|.*SortObject).*$</recordFilter>
                <fieldFilter>.*</fieldFilter>
                <recordNameSuffix>ClientDto</recordNameSuffix>
                <enumNameFilter>\b[A-Z0-9`_]+\b</enumNameFilter>
                <commonEnumNames>
                    <ProductDataStatus>ProductStatus</ProductDataStatus>
                    <ProductDataCurrency>ProductCurrency</ProductDataCurrency>
                </commonEnumNames>
                <defaultDataType>String</defaultDataType>
                <builderFieldsCount>3</builderFieldsCount>
            </configuration>
        </execution>
    </executions>
</plugin>
```
