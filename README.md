# simple-java-app

A tiny, harmless Java app used to test the **Endor Labs Package Firewall** with **Maven** and **Gradle**.

The app itself has no real function. It exists so that a build has real dependencies to download from Maven Central through the firewall, and so you can confirm that a known-malicious package gets blocked.

## What the app does

Each run prints a random programmer joke, followed by the same joke as JSON:

```
============================================================
                         Joke time!
============================================================
Q: Why do Java developers wear glasses?
A: Because they don't C#.

{
  "setup": "Why do Java developers wear glasses?",
  "punchline": "Because they don't C#.",
  "groanMeter": "*****",
  "reaction": "*slow clap*"
}
```

- The app takes no arguments and needs no input.
- It doesn't read files, make network calls, or use a database.
- `src/main/java/com/example/App.java` holds the jokes and the output logic.
- `src/test/java/com/example/AppTest.java` has 5 JUnit tests: the same random seed gives the same joke, jokes always come from the list, the groan meter has the right format, the banner shows its title, and the JSON has every field.

### Dependencies

All dependencies are published on Maven Central:

| Dependency | Version | Used for |
|---|---|---|
| `org.apache.commons:commons-lang3` | 3.17.0 | Banner text and the groan meter |
| `com.google.code.gson:gson` | 2.11.0 | JSON output |
| `org.junit.jupiter:junit-jupiter` | 5.11.3 | Tests only |

## Project files

| File | Purpose |
|---|---|
| `pom.xml` | Clean Maven build (expected to pass) |
| `pom_bad.xml` | Maven build that adds the blocked test package (expected to fail) |
| `build.gradle` | Clean Gradle build (expected to pass) |
| `build_bad.gradle` | Gradle build that adds the blocked test package (expected to fail) |
| `settings.xml` | Maven settings that send all downloads through the Endor firewall |
| `settings.gradle` | Gradle settings that send all downloads through the Endor firewall |

The "bad" files add `io.github.endorlabs:endor-java-webapp-demo:4.1`. Endor Labs flags this package as malware so it can be used to test the firewall. A working firewall blocks it with **403 Forbidden**.

## Prerequisites

- Java 17 or later
- Maven 3.9 or later
- Gradle 7.x (the bad-build command uses `-b`, which Gradle 8 removed)
- An Endor Labs API key and secret for your namespace, with Package Firewall enabled

## Setup

### 1. Set your credentials

Add your credentials to your shell profile, such as `~/.zshrc` or `~/.bash_profile`, then open a new terminal:

```bash
export ENDOR_API_KEY_ID="<api-key>"
export ENDOR_API_SECRET="<api-secret>"
```

Both settings files read these variables, so you never put the key in a project file.

### 2. Set your namespace

Both settings files contain a `<YOUR_NAMESPACE>` placeholder, marked with a `TODO` comment. Replace the whole placeholder, including the angle brackets, with your Endor Labs namespace.

**`settings.xml`:**

```xml
<url>https://factory.endorlabs.com/v1/namespaces/<YOUR_NAMESPACE>/firewall/maven/</url>
```

**`settings.gradle`** has two `url` lines (`pluginManagement` and `dependencyResolutionManagement`). Update both:



## Testing with Maven

`settings.xml` saves downloads to a separate cache, `.m2-repo/` inside this project. Nothing already cached in `~/.m2` gets reused, so every package goes through the firewall.

### Clean build (expected to pass)

```bash
mvn -s settings.xml clean package
mvn -s settings.xml -q exec:java
```

**Expected result:**
- `BUILD SUCCESS`, with `Tests run: 5, Failures: 0`
- The log shows downloads such as `Downloaded from endor-firewall: https://factory.endorlabs.com/v1/namespaces/<your-namespace>/firewall/maven/...`
- The app prints a joke

### Bad build (expected to fail)

```bash
mvn -s settings.xml -f pom_bad.xml dependency:resolve
```

**Expected result:** `BUILD FAILURE`, with an error like:

```
Could not transfer artifact io.github.endorlabs:endor-java-webapp-demo:pom:4.1 from/to endor-firewall
... status=403, title='Package blocked by Endor Labs Package Firewall',
detail='malware detected in io.github.endorlabs:endor-java-webapp-demo:4.1'
```

If the bad build **succeeds**, the firewall didn't block the package. Check your namespace and firewall policy in the Endor Labs console.

### Start fresh

```bash
rm -rf .m2-repo target
```

Maven also remembers failed downloads. To force it to ask the firewall again, add `-U`:

```bash
mvn -s settings.xml -U -f pom_bad.xml dependency:resolve
```

## Testing with Gradle

`settings.gradle` applies to both `build.gradle` and `build_bad.gradle`. It sends both plugin and dependency downloads through the firewall, and it rejects any repository that a build file adds on its own.

### Clean build (expected to pass)

```bash
gradle clean build --refresh-dependencies
gradle -q run
```

**Expected result:**
- `BUILD SUCCESSFUL`
- The app prints a joke


To see that the downloads came through the firewall:

```bash
gradle clean build --refresh-dependencies --info | grep factory.endorlabs.com
```

### Bad build (expected to fail)

```bash
gradle -b build_bad.gradle build --refresh-dependencies
```

**Expected result:** `BUILD FAILED`, with an error like:

```
Could not resolve io.github.endorlabs:endor-java-webapp-demo:4.1.
  > Could not GET 'https://factory.endorlabs.com/v1/namespaces/<your-namespace>/firewall/maven/io/github/endorlabs/endor-java-webapp-demo/4.1/endor-java-webapp-demo-4.1.pom'.
    Received status code 403 from server: Forbidden
```

`--refresh-dependencies` makes Gradle check with the firewall again instead of reusing its cache in `~/.gradle/caches`.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| 403 for clean packages such as `gson` or `commons-lang3` | Your credentials don't match the namespace, or Package Firewall isn't enabled for it. Check `echo $ENDOR_API_KEY_ID` and the namespace in both settings files. |
| 401 Unauthorized | `ENDOR_API_KEY_ID` or `ENDOR_API_SECRET` isn't set in this terminal. Open a new terminal after editing your shell profile. |
| Maven: `Non-parseable settings` | `<YOUR_NAMESPACE>` hasn't been replaced in `settings.xml`. |
| Bad build fails with "was cached in the local repository" | Maven remembered an earlier failure. Add `-U` or delete `.m2-repo`. |
| Gradle: `-b` not recognized | You're on Gradle 8 or later. Temporarily swap `build.gradle` and `build_bad.gradle`, or use Gradle 7.x. |
