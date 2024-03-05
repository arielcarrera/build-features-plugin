# build-features-plugin

Gradle plugin for managing dependencies as a platform

# README #

This repository contains the gradle Build Features Plugin used to build java projects.

The main purpose of this project is:

- provide an opinionated solution for centralized dependency management
- simplify configuration using "features" that can be enabled in the build script
- ensure a minimum set of common configurations/plugins to be applied
- reduce complexity and boilerplate in gradle build scripts
- ease maintenance for updating multiple projects to new dependency versions
- allow feature definition inline (in the build script) or globally managed in a custom extension/plugin project
- provide a simple way to work with spring docker-compose support and spring-cloud-config locally
- include additional common gradle tasks
- works with Spring's dependency-management gradle plugin under the hood for easy dependency resolution and versioning

Feature definition:

- each feature can contain one or more dependencies
- each dependency has its own version that can be easily overridden by a property
- each feature can apply conditionals to the inclusion of each dependency based on the existence of other active features

### Plugins ###

There are 2 different plugins, one intended to be used for building services relying on the Spring/SpringBoot stack and other one for
building libraries.

> For Libraries - Plugin id: 'io.github.arielcarrera.build.library'

> For Services - Plugin id: 'io.github.arielcarrera.build.boot'

### Build ###

| Command            | Description                                 |
|--------------------|---------------------------------------------|
| make               | Build the artifact and publish the plugin   |
| make build         | Build the artifact                          |
| make publish       | Publish artifact to remote Maven Repository |
| make publish-local | Publish artifact to local Maven Repository  |
| make clean         | Clean up the project                        |
| make test          | Run an artifact test using gradlew          |
| make refresh       | Build the artifact with --refresh-dep param |

### Tasks ###

| Command                             | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | 
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| gradlew version                     | Gets the artifact/app version                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | 
| gradlew buildFeatures               | Builds the related build-features project pointed by environment variable BUILD_FEATURES_REPO or property 'buildFeaturePath'.<br/>**Options:**<br/>publish: Indicates if the build-features project is published<br/>publishToMavenLocal : Indicates if the build-features project is published to local maven repository<br/>buildFeaturePath=PATH :  Sets the directory/path of the related build-features project                                                                                           | 
| gradlew exportFeature               | Generates / exports a feature definition by filtering the list of dependencies by a given text value<br/>**Options:**<br/>dependency=VALUE : Sets a text value for filtering the dependencies to be exported<br/>name=VALUE : Sets the feature name<br/>desc=VALUE : Sets the feature description<br/>property=VALUE : Sets the property name to allow overwriting of the version number<br/>path=VALUE : Sets the path of the related build features project<br/>f : Forces / overrides the output file |
| gradlew listDependencies            | Lists the project dependencies<br/>**Options:**<br/>all : Lists all the dependencies                                                                                                                                                                                                                                                                                                                                                                                                                               |
| gradlew publishFeatures             | Builds and publishes the build-features project<br/>**Options:**<br/>path=VALUE : Sets the path of the related build features project                                                                                                                                                                                                                                                                                                                                                                              |
| gradlew publishFeaturesToMavenLocal | Builds and publishes the build-features project to local maven repository<br/>**Options:**<br/>path=VALUE : Sets the path of the related build features project                                                                                                                                                                                                                                                                                                                                                    |

### Bundled Plugins ###

Both plugins, internally includes the following plugins:

1. Java / Java Library
2. Spring Management Dependencies
3. Maven Publish
4. JaCoCo
5. JaCoCo Log (summary)

The SpringBoot Build Plugin also includes:

1. Spring Boot Plugin

### Repositories ###

By default, the plugin takes the environment variables NEXUS_URL, NEXUS_USER and NEXUS_PASS and creates the maven
repositories for you.
If you need to override the repositories you can define your own repository using the following names:
**releasesRepository** for releases and **snapshotsRepository** for snapshots.
If the user defined a repository with the same URL, the repository creation is skipped.

### Managed Versions ###

Spring Boot dependencies are defined by the usage of the Spring Boot gradle plugin and the optional inclusion of the Spring Cloud
BOM. Although they have a default build-time version, these can be overridden using the following properties:

```groovy
ext {
    set('springBootVersion', '3.2.1')
    set('springDependencyManagementVersion', '1.1.3')
    set('springCloudVersion', '2023.0.0')
}
```

Additional dependencies should be defined in a constants class:

```java
public final class DefaultVersions {
    public static final String ASM_VERSION = "9.5";
    public static final String AWS_JAVA_SDK_VERSION = "1.12.498";
    public static final String CHAOS_MONKEY_SPRING_BOOT_VERSION = "3.0.1";
    public static final String JAVASSIST_VERSION = "3.29.2-GA";
    public static final String LOGSTASH_LOGBACK_VERSION = "7.4";
    public static final String SHEDLOCK_VERSION = "5.5.0";
    public static final String SPRING_CLOUD_VERSION = "2022.0.3";
    public static final String SPRINGDOC_STARTER_VERSION = "2.1.0";
    public static final String SWAGGER_VERSION = "2.2.12";
    public static final String ZIPKIN_BRAVE_VERSION = "5.16.0";
}
```

Additional dependency versions can be overridden by defining variables in the target project. For example:

```groovy
ext {
    set('springCloudVersion', '2022.0.3')
    set('asmVersion', '9.5')
    set('awsJavaSdkVersion', '1.12.496')
    set('chaosMonkeySpringBootVersion', '3.0.1')
    set('javassistVersion', '3.29.2-GA')
    set('logbackLogstashVersion', '7.4')
    set('springdocVersion', '2.1.0')
    set('swaggerVersion', '2.2.12')
    set('zipkingVersion', '5.16.0')
}
```

### Plugin Settings ###

> The plugin settings are enclosed in the **buildFeatures** section.

#### Common plugin settings:

| Command                      | Description                              | Default Value | Example               |
|------------------------------|------------------------------------------|---------------|-----------------------|
| javaVersion                  | Java language version                    | '17'          | '21'                  |
| defaultSpringCloudVersion    | Default Spring Cloud BOM version         | '2023.0.0'    | '2023.0.1'            |
| importSpringCloudBomEnabled  | Imports the Spring Cloud BOM             | true          | false                 |
| publishEnabled               | Publishes the project artifact           | true          | false                 |
| artifactId                   | The artifact Identifier                  | -             | 'my-artifact'         |
| testCoverageEnabled          | Enables the test coverage plugin         | false         | true                  |
| testCoverageExclusions       | Adds test coverage class exclusions      | -             | '\*\*/exception/\*\*' |
| testCoverageMinimumThreshold | Sets the minimum test coverage threshold | -             | '0.9'                 |                  |

#### Library plugin settings:

| Command                  | Description                     | Default Value | Example |
|--------------------------|---------------------------------|---------------|---------|
| defaultSpringBootVersion | Default Spring Boot BOM version | '3.2.1'       | '3.1.2' |
| importSpringBootBom      | Imports the Spring Boot BOM     | true          | false   |

#### SpringBoot plugin settings:

| Command              | Description                                                                 | Default Value  | Example               |
|----------------------|-----------------------------------------------------------------------------|----------------|-----------------------|
| envFile              | Alternative envFile location/name                                           | '.env'         | 'alternative.env'     |
| showEnvVars          | Print environment variables (.env file)                                     | true           | false                 |
| secretVariableNames  | Additional secret variable names to "PASSWORD", "PASS", "APIKEY", "API_KEY" | []             | ['USER']              |
| dockerComposeEnabled | Build the artifact with --refresh-dep param                                 | true           | false                 |
| dockerComposeFile    | Docker compose file name                                                    | 'compose.yaml' | 'docker-compose.yaml' |
| dockerComposeName    | Docker compose stack name                                                   | 'api-config'   | 'custom'              |
| dockerComposeProject | Docker compose project name                                                 | 'app-config'   | 'custom'              |

#### Features:

By default, the build-features-plugin project does not contain any feature definition.

> The feature definitions must be placed locally in the build script or globally managed (as platform) in a child plugin project
> that extends the build-features-plugin.

##### Local definition:

```groovy
    definitions {
        feature('featureId', 'Feature Name', 'versionProperty') {
            dependency('example:artifact:123-VERSION') {
                exclude('example2:artifact2')
                exclude('example3:artifact3')
            }
            dependency('implementation', 'example4:artifact4:123-VERSION')
            testDependency('example5:artifact5:123')
        }
    }
```

##### Global definition:

Custom file in the resource folder of a child project that extends build-features-plugin of content like this:

````groovy
package buildFeatures

feature('apacheCommonsIo', 'Apache Commons IO') {
    implementation('commons-io:commons-io:%COMMONS_IO_VERSION', 'commonsIoVersion')
}
````

##### Feature activation:

Each feature can be enabled/disabled by name in the **features** section. For example:

```groovy
features {
    enable 'apacheCommonsIo'
}
```

### Complementary tasks ###

1. Version

Returns the project version.

```shell
./gradlew version
```

Example:

```shell
./gradlew version

> Task :version
0.3.1
```

> You can use --quiet parameter to avoid log messages
>```shell
>./gradlew version --quiet
>0.3.1
>```

2. **exportFeature**

This task finds a feature in the current Gradle build script and returns a candidate feature definition in console, local build
features project folder or local files if no build features project location was configured.

> By default, it finds the dependency in the dependencies section of the current gradle file, takes the path argument or the
> environment variable BUILD_FEATURES_REPO,
> and generates the feature file in the feature's folder. It also adds the default version in the property file if missing and
> executes a gradle 'build publishToLocalMaven' in the build features repository.
> When a path argument or the environment variable with the location of the feature build project is not provided, it generates a
> .gradle file and a .properties file in the current working directory.

Env Var:

- BUILD_FEATURES_REPO (location of the build features project). OPTIONAL.

Arguments:

- dependency (artifact name or text for filter by). REQUIRED.
- name (feature name). OPTIONAL.
- desc (feature description). OPTIONAL.
- property (version property). OPTIONAL.
- f (force file overwrite). OPTIONAL.
- path (location of the build features project). OPTIONAL.

Example:

```shell
./gradlew exportFeature --dependency=lambda --name='systemLambda' --f

> Task :exportFeature
properties:
SYSTEM_LAMBDA_VERSION='1.2.1'

feature:
package buildFeatures
feature('systemLambda', 'System Lambda') {
implementation('com.github.stefanbirkner:system-lambda:%SYSTEM_LAMBDA_VERSION', 'systemLambdaVersion')
}
```

3. **publishFeatures**

This task executes a 'publish' task in the given build features project.

Env Var:

- BUILD_FEATURES_REPO (location of the build features project). OPTIONAL.

Arguments:

- path (location of the build features project). OPTIONAL.

Example:

```shell
./gradlew publishFeatures
```

4. **publishFeaturesToMavenLocal**

This task executes a 'publishToMavenLocal' task in the given build features project.

Env Var:

- BUILD_FEATURES_REPO (location of the build features project). OPTIONAL.

Arguments:

- path (location of the build features project). OPTIONAL.

Example:

```shell
./gradlew publishFeaturesToMavenLocal
```

5. **buildFeatures**

This task executes a 'build' task in the given build features project.

Env Var:

- BUILD_FEATURES_REPO (location of the build features project). OPTIONAL.

Arguments:

- path (location of the build features project). OPTIONAL.
- publish (publishes the 'build features' to remote maven repository). OPTIONAL.
- publishToMavenLocal (publishes the 'build features' to local maven repository). OPTIONAL.

Example:

```shell
./gradlew buildFeatures --publish
```


### Usage ###

Ensure the file **settings.gradle** exists and contains the plugin repository definition:

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url System.getenv("NEXUS_URL") + 'maven-releases/'
            credentials {
                username System.getenv("NEXUS_USER")
                password System.getenv("NEXUS_PASS")
            }
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url System.getenv("NEXUS_URL") + 'maven-snapshots/'
            credentials {
                username System.getenv("NEXUS_USER")
                password System.getenv("NEXUS_PASS")
            }
            mavenContent {
                snapshotsOnly()
            }
        }
        gradlePluginPortal()
    }
}
rootProject.name = 'project-name'
```

Edit the file **build.gradle**:

1. Include plugin definition:

```groovy
plugins {
    id 'io.github.arielcarrera.build.boot' version '1.0.0'
}
```

2. Add **buildFeatures** settings:

```groovy
buildFeatures {
    envFile = ".env"
    showEnvVarsEnabled = true
    secretVariableNames = ['USER']
    defaultSpringCloudVersion = '2022.0.3'
    importSpringCloudBomEnabled = true
    dockerComposeFile = 'compose.yaml'
    dockerComposeName = 'app-config'
    appConfigServiceName = 'app-config'
    features {
        //Spring
        enable 'springBootActuator'
        enable 'springBootDataMongoDb'
        enable 'springBootDockerComposeSupport'
        enable 'springBootJaxRs'
        enable 'springBootTestSupport'
        enable 'springBootValidation'
        enable 'springBootWeb'
        enable 'springBootWebflux'
        enable 'springCloudConfig'
        enable 'springKafka'
        enable 'springRetry'
        //Aws
        enable 'awsCognito'
        enable 'awsS3'
        //Chaos Monkey Spring Boot
        enable 'chaosMonkeySpringBoot'
        //OpenApi
        enable 'openApi'
        //Logstash
        enable 'logstashEncoder'
        //Shedlock
        enable 'shedlockMongo'
    }
}
```

3. (optional) Override default dependency versions:

```groovy
ext {
    set('springCloudVersion', '2022.0.3')
    set('awsJavaSdkVersion', '1.12.496')
    set('logbackLogstashVersion', '7.4')
    set('springdocVersion', '2.1.0')
    set('swaggerVersion', '2.2.12')

    // ... other springBoot dependencies versions allowed...
    // Example:
    //set('hibernate.version', '6.2.6.Final')
}
```

4. (optional) Add custom dependencies:

```groovy
dependencies {
    //Custom dependencies
    //Example: 
    implementation "org.springdoc:springdoc-openapi-starter-webflux-ui:1.0.0"
}
```

5. (optional) After adding some new dependencies, you may want to promote them to a feature for easier reuse. In these cases, you
   can do it manually or you can execute the following steps:
   1. Export a new feature::
   ```shell
    ./gradlew exportFeature --dependency=springdoc --name='springdoc' --r
    
    > Task :exportFeature
    properties:
    SPRINGDOC_VERSION='1.0.0'
    
    feature:
    package buildFeatures
    feature('springdoc', 'Springdoc') {
    implementation('org.springdoc:springdoc-openapi-starter-webflux-ui:%SPRINGDOC_VERSION', 'springdocVersion')
    }
   ```
   2. Promote feature to features project and build the features project locally:
   ```shell
   ./gradlew buildFeatures --publishToMavenLocal
   ```
   3. Publish features to remote maven repository:
   ```shell
   ./gradlew publishFeatures
   ```


### Publishing ###

The publishing is enabled/disabled with the setting *publishEnabled*. If the publishing is enabled, the setting **artifactId** is also
required.

Example:

```groovy
buildFeatures {
    settings {
        publishEnabled = true
        artifactId = 'project-name'
    }
}
```

### Test ###

By default, the Junit Platform is executed when at least a test exists.

### Test Coverage / Reporting ###

By default, the test coverage is disabled. It can be enabled using the setting *testCoverageEnabled*.

Configuration example:

```groovy
buildFeatures {
    settings {
        testCoverageEnabled = true
        testCoverageExclusions = ['**/exception/**']
        testCoverageMinimumThreshold = 0.4
    }
}
```

### Docker compose support ###

By default, for the SpringBoot Build Plugin, it starts a given Spring Cloud Config container for allowing to the service to
start with the given configuration.

#### Start App Config manually:
```shell
./gradlew runAppConfig
```

#### Stop App Config manually:
```shell
./gradlew stopAppConfig
```

### Author
* Ariel Carrera (carreraariel@gmail.com)