# jackson-json-schema-maven
Maven plugin for jackson-jsonSchema

## Usage

```
<build>
    <plugins>
        <plugin>
            <groupId>com.devialab</groupId>
            <artifactId>jackson-json-schema-maven</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <configuration>
                <outputDirectory>${project.build.outputDirectory}</outputDirectory>
                <fileName>context.jsonSchema</fileName>
                <packageName>com.example.domain</packageName>
                <contentLocation>http://example.com/context-jsonSchema/</contentLocation>
            </configuration>
            <executions>
                <execution>
                    <phase>generate-test-sources</phase>
                    <goals>
                        <goal>generate-context</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```