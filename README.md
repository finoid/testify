# testify

A Java library designed to streamline testing by integrating Testcontainers, data fakers, test fixtures, snapshot
testing, and more - enabling efficient test automation.

<div align="center">
  <img src=".github/assets/finoid-testify.png" width="256" alt="Testify Library Logo"/>
  <p style="font-size: 10px">
  </p>
</div>

## Modules

* **testify-core** - Core module providing data fakers, and JSON serialization utilities.
* **testify-snapshot** - Snapshot module providing snapshot testing utilities
* **testify-spring** - Spring module for end-to-end (E2E), integration, and API testing.
* **testify-spring-autoconfigure** - Spring Auto-Configuration module for automatically bootstrapping and configuring beans from other Testify modules.
* **testify-testcontainers** - Testcontainers support, including JUnit extensions for MsSqlServer.