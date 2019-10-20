[![CircleCI](https://circleci.com/gh/KuliginStepan/mongration/tree/master.svg?style=shield)](https://circleci.com/gh/KuliginStepan/mongration/tree/master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ffcf51506af84115ae91ab54cb437f08)](https://www.codacy.com/app/KuliginStepan/mongration?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=KuliginStepan/mongration&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/KuliginStepan/mongration/branch/master/graph/badge.svg)](https://codecov.io/gh/KuliginStepan/mongration)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.kuliginstepan/mongration.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.kuliginstepan%22%20AND%20a:%22mongration%22)
[![Licence](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/kuliginstepan/mongration/blob/master/LICENSE)

# Mongration
MongoDB data migration tool for Spring Boot projects

## Key features
*   Support Spring Boot lifecycle
*   Changelogs are regular spring beans
*   Support MongoDB transactions
*   Support ReactiveMongoTemplate

## Getting started
### Add a dependency
#### Maven
```xml
<dependency>
  <groupId>com.github.kuliginstepan</groupId>
  <artifactId>mongration</artifactId>
  <version>version</version>
</dependency>
```
#### Gradle
```groovy
compile("com.github.kuliginstepan:mongration:version")
```
or 
```groovy
implementation 'com.github.kuliginstepan:mongration:version'
```

### Configuration properties
*   `mongration.enabled` – enable or disable mongration. Default to `true`
*   `mongration.changelogs-collection` – collection for saving changesets. Defaults to `mongration_changelogs`
*   `mongration.mode` – mode for executing changesets. Defaults to `AUTO`, means that mongration will try to analyze 
changesets to choose proper mode. Possible modes are `IMPERATIVE`, `REACTIVE`

### Changelog
To mark class as a changelog you need to annotate it with `@Changelog`. This annotation makes class regular spring bean.
`@Changelog` has property `id`, which is a simple class name by default.

### Changeset
To mark method as a changeset you need to annotate it with `@Changeset`. By default changeset's id is a method name.
Changelog collection has compound unique index `@CompoundIndex(def = "{'changeset': 1, 'changelog': 1}", unique = true)` 
to check if changeset executed or not.

You can inject all beans available in bean factory as a changeset's method arguments.

Imperative changeset method must have `void` return type and reactive changeset method must returns `Mono<Void>`.

If you configured [MongoDB](https://docs.mongodb.com/manual/tutorial/deploy-replica-set/) and 
[Spring](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.transactions) to enable 
transaction support, changesets may be executed in transactions. 

### Spring Boot Actuator
Mongration provides actuator endpoint `mongration`, which lists all executed changesets.

### Migration from old versions
*   drop changelog table
*   remove all executed changesets