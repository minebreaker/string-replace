# Java String-replace annotation processor

The annotation processor that replace the value of the filed which is annotated by `@Replace` at compile time.


## Example

See [https://github.com/minebreaker/string-replace/tree/master/sample]() for the detail

```java
class Target {
    @Replace("KEY")
    public static String field = "";
}
```

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


## Limitations

It uses internal jdk packages, so can not be used with some JDK implementations(Eclipse ECJ?).


## License

MIT
