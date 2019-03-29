# Java String-replace annotation processor

The annotation processor that replaces the value of the filed which is annotated by `@Replace` at compile time.


## Example

See [https://github.com/minebreaker/string-replace/tree/master/sample]() for the detail

In your Java source:

```java
class Target {
    @Replace("KEY")
    public static String field = "";
}
```

Gradle:

```gradle
compileJava.options.compilerArgs += ["-Arip.deadcode.javac.stringreplace.properties=key=value"]
```

The value of the `field` is converted to `value` on compilation.

```java
class Target {
    @Replace("KEY")
    public static String field = "value";
}
```


## Options

See [https://docs.oracle.com/en/java/javase/12/tools/javac.html]()


#### `rip.deadcode.javac.stringreplace.properties`

A comma separated list of the `$key=$value` pairs to replace the filed values.

Example: `foo1=bar1,foo2=bar2`


#### `rip.deadcode.javac.stringreplace.requireInitializer`

Checks if an initializer(default value for the field) is set.
This can be problematic, because the annotation processor is not used due to some misconfiguration, the lack of the initializer may cause a NPE.
Set `false` to disable.

```
// Bad:
public static final String FIELD;

// Good:
public static final String FIELD = "default";
```

#### `rip.deadcode.javac.stringreplace.checkStaticFinal`

Checks if the field is `static` and `final`.
This is generally a good practice.
Set `false` to disable.


## TODO

* tests
* escape for '=' and ','


## Limitations

It uses internal jdk packages, so can not be used with some JDK implementations(Eclipse ECJ?).


## Build

### IntelliJ tips

* add `--add-exports` to javac settings for Java 9+ (c.f. `build.gradle`)


## License

MIT
