# testify-testcontainers

### Testcontainers

#### Enable re-usable containers to speed up test suites

1. Run the tests at least once to generate the `.testcontainers.properties` file.
2. Navigate to your user/home directory and open the `.testcontainers.properties` file.

```
Linux: /home/<USER>/.testcontainers.properties
Windows: C:/Users/<USER>/.testcontainers.properties
MacOS: /Users/<USER>/.testcontainers.properties
```

3. Add `testcontainers.reuse.enable=true` at the end of the file.
4. Save and close the file.