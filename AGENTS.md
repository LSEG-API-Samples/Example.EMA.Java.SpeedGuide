# AGENTS.md

JavaFX desktop tool that browses LSEG market data "speed guides" using the EMA (Enterprise Message API) Java consumer. Demonstration tool only; see [README.md](README.md) for user-facing docs and command-line options.

## Stack
- Java 11 (`maven.compiler.source/target`), Maven, JavaFX 17.0.18, EMA `com.refinitiv.ema` 3.10.1.0
- No test suite exists

## Build and run
- Build: `mvn clean package` (produces shaded `target/SpeedGuide.jar` and a distribution zip via `src/main/assembly/distribution.xml`)
- Run (JavaFX is not bundled; point at the local SDK in `javafx-sdk-17.0.18/`):
  `java --module-path ".\javafx-sdk-17.0.18\lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics -jar ".\target\SpeedGuide.jar" [options]`
- `--h` prints all connection options; `--d` enables debug output

## Layout
- `src/main/java/com/lseg/ema/example/gui/`
  - `SpeedGuide.java`: `Application` entry point; parses the command line, loads FXML, starts the consumer thread
  - `SpeedGuideConsumer.java` / `ConsumerClient.java`: EMA consumer and message handling
  - `StatusLogHandler.java`: status/log output
  - `view/`: FXML, CSS, images, and controllers (`SpeedGuideViewController`, `SpeedGuideConnection`)
- `src/main/filtered-resources/app.properties`: Maven-filtered; carries the pom version
- `src/main/resources/`: logos and splash images (unfiltered)

## Conventions and gotchas
- **Version**: single source is `<version>` in [pom.xml](pom.xml). `SpeedGuide.VER_CODE` reads it at runtime from `/app.properties`; do not hard-code it. It shows `unknown` unless Maven has built the project.
- **View resources are duplicated by the build**: the `copy-resources` executions copy `.fxml`, `.css` and `.png` from `src/main/java/.../gui/view` into `src/main/resources/view` during `generate-resources`. Edit the files under `src/main/java/.../gui/view`, not the generated copies in `src/main/resources/view`.
- Do not enable filtering on `src/main/resources`; it would corrupt the binary image files.
- FXML is loaded by classpath path (`/view/SpeedGuideView.fxml`, `/view/ConnectionDialog.fxml`); keep these paths in sync with the resource copy.
- Do not commit credentials (machine IDs, passwords, client secrets, app keys); the values in README and help text are placeholders.
- `target/` and `javafx-sdk-*` are local artifacts; do not edit them.
