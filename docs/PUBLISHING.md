# Publishing streamersonglist4k

`streamersonglist4k` is intended to be released through JitPack as:

```kotlin
implementation("com.github.mbayou:streamersonglist4k:<version>")
```

## Release Checklist

1. Pick the version.
   - Update `version` in [build.gradle.kts](../build.gradle.kts).
   - Update README snippets if the public API changed.
2. Run verification.
   - `./gradlew clean test --console=plain`
3. Commit the release changes.
4. Tag the commit.
   - `git tag 0.2.3 && git tag v0.2.3`
   - `git push origin 0.2.3 v0.2.3`
5. Trigger JitPack.
   - Open `https://jitpack.io/#mbayou/streamersonglist4k`
   - Select the tag and click **Get it**.
   - Confirm the build succeeds on Java 21.

## Notes

- Do not publish ai_licia application logic in this repository.
- Keep generated OpenAPI changes reviewed manually before exposing them as public Kotlin types.
- Prefer adding typed request/response models over `Map<String, Any?>` public APIs.
