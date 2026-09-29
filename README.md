# TP-IYCS-GIT

## Local database configuration

Requirements: JDK 21 and a running MySQL instance with the application database.
Maven is provided through the Maven Wrapper included in this repository.

1. From the project root, create your local configuration file (only if it does
   not already exist):

   ```bash
   cp application-local.properties.example application-local.properties
   ```

2. Edit `application-local.properties` with your database URL, username and
   password. Keep machine-specific settings in this file instead of changing
   `src/main/resources/application.properties`.

3. Run the application from the project root:

   ```bash
   bash ./mvnw spring-boot:run
   ```

   On Windows, use `mvnw.cmd spring-boot:run`. When running from an IDE, set its
   working directory to the project root.

Spring Boot explicitly imports `application-local.properties` from the working
directory through `spring.config.import`; no profile activation is required.
The import is optional so other environments can provide connection settings
through environment variables (`SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`). A MySQL connection
is still required to run the application.

The local file is ignored by Git and stays outside `src/main/resources`, so it
is not included in the application JAR. The example file is versioned and must
contain placeholders only. Shared configuration stays versioned in
`src/main/resources/application.properties`. Adding a file to `.gitignore` does
not stop tracking a file that has already been committed.

Check the ignore rule with:

```bash
git check-ignore -v application-local.properties
```

## Verification

Run from the project root:

```bash
bash ./mvnw -B clean verify
```

On Windows, use `mvnw.cmd -B clean verify`.
Tests use their own configuration in `src/test/resources/application.properties`
and an in-memory H2 database. They do not require MySQL or the local settings file.
