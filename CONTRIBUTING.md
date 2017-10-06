# Contributing guidelines

## Code formatting

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). See
https://github.com/google/google-java-format for IDE plugins. The rules are not configurable.

The build will fail if the code is not formatted. To format all files from the command line, run:
 
```
mvn fmt:format -Dformat.validateOnly=false
```

Some aspects are not covered by the formatter:
* imports: please configure your IDE to follow the guide (no wildcard imports, normal imports 
  in ASCII sort order come first, followed by a blank line, followed by static imports in ASCII
  sort order).
* XML files: indent with two spaces and try to respect the column limit of 100 characters.

## Coding style

Avoid static imports, with those exceptions:
* AssertJ's `assertThat` / `fail` in unit tests.

Tests:
* test methods names use lower snake case, generally start with "should" and clearly indicate the
  purpose of the test, for example: `should_fail_if_key_already_exists`. If you have trouble coming
  up with a simple name, it might be a sign that your method does too much and should be split.
* we use AssertJ (`assertThat`). Don't use JUnit's assertions (`assertEquals`, `assertNull`, etc).

## License headers

The build will fail if some license headers are missing. To update all files from the command line,
run:

```
mvn license:format
```

## Pre-commit hook (highly recommended)
 
Ensure `pre-commit.sh` is executable, then run:

```
ln -s ../../pre-commit.sh .git/hooks/pre-commit
```

This will only allow commits if the tests pass. It is also a good reminder to keep the test suite
short. 
