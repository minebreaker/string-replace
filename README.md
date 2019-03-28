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


## TODO

* tests
* checkDefaultInitializer flag
* checkFinal flag


## Limitations

It uses internal jdk packages, so can not be used with some JDK implementations(Eclipse ECJ?).


## Build

### IntelliJ tips

* add `--add-exports` to javac settings for Java 9+ (c.f. `build.gradle`)
* Updating to IDEA 2019.1 sets default test runner to Gradle. This can be problem.
    * Somehow Gradle test runner fails to grab tools.jar. IDEA test runner will be fine.
    * In that case, add tools.jar to project library manually.


## License

MIT
