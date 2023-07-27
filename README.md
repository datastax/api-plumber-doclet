# API Plumber doclet

A custom doclet to check that an API doesn't leak a set of "forbidden" packages.

## Why

* coding conventions: Java's visibility system lacks flexibility. One workaround is to use a naming
  convention with `api` / `internal` root packages. The public API should not leak any internal
  types.
* shaded libraries: the API should not leak any shaded types.

## Usage

### Command line

```
javadoc -preventleak com.package1 [-preventleak com.package2] \
        -doclet com.datastax.oss.doclet.ApiPlumber ...
```

### Maven

```xml
<plugin>
  <artifactId>maven-javadoc-plugin</artifactId>
  <executions>
    <execution>
      <id>check-api-leaks</id>
      <goals><goal>javadoc</goal></goals>
      <phase>process-classes</phase>
      <configuration>
        <doclet>com.datastax.oss.doclet.ApiPlumber</doclet>
        <docletArtifact>
          <groupId>com.datastax.oss</groupId>
          <artifactId>api-plumber-doclet</artifactId>
          <version>...</version>
        </docletArtifact>
        <additionalJOptions>
          <additionalparam>-preventleak</additionalparam>
          <additionalparam>com.package1</additionalparam>
          <additionalparam>-preventleak</additionalparam>
          <additionalparam>com.package2</additionalparam>
        </additionalJOptions>
        <useStandardDocletOptions>false</useStandardDocletOptions>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Violations are printed on the standard error, and cause the command to return with a non-zero exit
value.

You can ignore elements with a javadoc tag:

```java
/** @leaks-private-api */
public com.example.internal.InternalType myField;
```

Note: due to breaking changes in the Doclet API, 2.x branch and releases 2.0.0+ require minimum of Java 9. The 1.x branch and 1.0.0 release support Java 8 and lower only. This is the case for both development in this repo, and for adopters using the `api-plumber-doclet` in their own projects.

For more info see the [OpenJDK intro to the new API](https://openjdk.org/groups/compiler/using-new-doclet.html) and the [jdk.javadoc.doclet documentation](https://docs.oracle.com/javase%2F9%2Fdocs%2Fapi%2F%2F/jdk/javadoc/doclet/package-summary.html).