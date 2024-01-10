# build-features-plugin
Gradle plugin for managing dependencies as a platform

# README #

This repository contains the gradle Build Features Plugin used to build java projects.

The main purpose of this project is:

- provide an opinionated solution for centralized dependency management
- provide dependencies as features that can be enabled using feature flags
- ensure a minimum set of common configurations/plugins to be applied
- provide a simple way to work with spring docker-compose support and spring-cloud-config locally
- reduce complexity and boilerplate in gradle build scripts
- ease maintenance for updating multiple projects to new dependency versions
- allow customization, override versions and include custom features/dependencies

### Building ###

| Command            | Description                                 |
|--------------------|---------------------------------------------|
| make               | Build the artifact and publish the plugin   |
| make build         | Build the artifact                          |
| make publish       | Publish artifact to remote Maven Repository |
| make publish-local | Publish artifact to local Maven Repository  |
| make clean         | Clean up the project                        |
| make test          | Run an artifact test using gradlew          |
| make refresh       | Build the artifact with --refresh-dep param |

### Managed Versions ###

Spring Boot dependencies are defined by the build of the plugin and defined in the *build.gradle* file.

```groovy
ext {
    set('springBootVersion', '3.1.2')
    set('springDependencyManagementVersion', '1.1.2')
}
```

Additional dependencies are defined in the DefaultVersions.class:

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

Additional dependency versions can be overridden by defining variables in the target project:

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

#### Global buildFeatures settings:

| Command                     | Description                                                                 | Default Value  | Example                   |
|-----------------------------|-----------------------------------------------------------------------------|----------------|---------------------------|
| envFile                     | Alternative envFile location/name                                           | '.env'         | 'alternative.env'         |
| showEnvVarsEnabled          | Print environment variables (.env file)                                     | true           | false                     |
| secretVariableNames         | Additional secret variable names to "PASSWORD", "PASS", "APIKEY", "API_KEY" | []             | ['USER']                  |
| defaultSpringCloudVersion   | Spring cloud Version                                                        | '2022.0.3'     | 2022.0.1'                 |
| importSpringCloudBomEnabled | Clean up using gradlew                                                      | true           | false                     |
| dockerComposeFile           | Build the artifact with --refresh-dep param                                 | 'compose.yaml' | 'docker-compose.yaml'     |
| dockerComposeName           | Run an artifact test using gradlew                                          | 'api-config'   | 'custom'                  |
| features                    | Feature flags                                                               | {}             | [View](#feature-settings) |

#### Feature settings

| Command                        | Description                                   | Default Value | Example |
|--------------------------------|-----------------------------------------------|---------------|---------|
| shedlockMongo                  | Add Shedlock Spring & Mongo                   | false         | true    |
| springBootActuator             | Add SpringBoot Actuator                       | false         | true    |
| springBootDataMongoDb          | Add SpringBoot MongoDb                        | false         | true    |
| springBootDockerComposeSupport | Add SpringBoot Docker Comppose Support        | false         | true    |
| springBootJaxRs                | Add SpringBoot Jersey JAX-RS & SpringBoot Web | false         | true    |
| springBootTestSupport          | Add SpringBoot Test Support                   | false         | true    |
| springBootValidation           | Add SpringBoot Validation                     | false         | true    |
| springBootWeb                  | Add SpringBoot Web                            | false         | true    |
| springBootWebflux              | Add SpringBoot Webflux                        | false         | true    |
| springCloudConfig              | Add Spring Cloud Config Client                | false         | true    |
| springRetry                    | Add Spring Retry                              | false         | true    |
| awsCognito                     | Add AWS JAVA SDK & Cognito                    | false         | true    |
| awsS3                          | Add AWS JAVA SDK & S3                         | false         | true    |
| openApi                        | Add OpenApi api                               | false         | true    |
| logstashEncoder                | Add Logback Logstash encoder                  | false         | true    |

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
    id 'com.github.arielcarrera.build.boot' version '1.0.0'
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
        springBootActuator = true
        springBootDataMongoDb = true
        springBootDockerComposeSupport = true
        springBootJaxRs = true
        springBootTestSupport = true
        springBootValidation = true
        springBootWeb = true
        springBootWebflux = true
        springCloudConfig = true
        springKafka = true
        springRetry = true
        //Aws
        awsCognito = true
        awsS3 = true
        //Chaos Monkey Spring Boot
        chaosMonkeySpringBoot = true
        //OpenApi
        openApi = true
        //Logstash
        logstashEncoder = true
        //Shedlock
        shedlockMongo = true
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
    //implementation "org.springdoc:springdoc-openapi-starter-webflux-ui:$springdocVersion"
}
```

### More tasks ###

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

This task finds a feature in the current Gradle build script and returns a candidate feature definition in console, local build features project folder or local files if no build features project location was configured.

> By default, it finds the dependency in the dependencies section of the current gradle file, takes the path argument or the environment variable BUILD_FEATURES_REPO,
> and generates the feature file in the feature's folder. It also adds the default version in the property file if missing and executes a gradle 'build publishToLocalMaven' in the build features repository. 
> When a path argument or the environment variable with the location of the feature build project is not provided, it generates a .gradle file and a .properties file in the current working directory.

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
./gradlew exportFeature --dependency=lambda --name='systemLambda' --r

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

### Author
* Ariel Carrera (carreraariel@gmail.com)