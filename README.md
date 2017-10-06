# API Plumber doclet

A custom doclet to check that an API doesn't leak a set of "forbidden" packages.

## Why

* coding conventions: Java's visibility system lacks flexibility. One workaround is to use a naming
  convention with `api` / `internal` root packages. The public API should not leak any internal
  types.
* shaded libraries: the API should not leak any shaded types.

## Usage

```
javadoc -preventleak com.package1 [-preventleak com.package2] \
        -doclet com.datastax.oss.doclet.ApiPlumber ...
```

Violations are printed on the standard error, and cause the command to return with a non-zero exit
value.

You can ignore elements with a javadoc tag: 

```java
/** @leaks-private-api */
public com.example.internal.InternalType myField;
```