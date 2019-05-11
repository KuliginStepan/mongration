[![CircleCI](https://circleci.com/gh/KuliginStepan/mongration/tree/master.svg?style=shield)](https://circleci.com/gh/KuliginStepan/mongration/tree/master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ffcf51506af84115ae91ab54cb437f08)](https://www.codacy.com/app/KuliginStepan/mongration?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=KuliginStepan/mongration&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/KuliginStepan/mongration/branch/master/graph/badge.svg)](https://codecov.io/gh/KuliginStepan/mongration)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.kuliginstepan/mongration.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.kuliginstepan%22%20AND%20a:%22mongration%22)
[![Licence](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/kuliginstepan/mongration/blob/master/LICENSE)

# Mongration
MongoDB data migration tool for Spring Boot projects

## Key features
*   Support Spring Boot lifecycle
*   ChangeLogs are regular spring beans
*   Support mongoDB transactions

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
`compile("com.github.kuliginstepan:mongration:version")`

or 

`implementation 'com.github.kuliginstepan:mongration:version'`
### Configuration
Mongration provides default configuration. You may change collection name where mongration will save executed changesets.
To change it add property `mongration.changelogs-collection` with custom collection name. By default mongration will save
executed changesets in collection named `mongration_changelogs`. If you won\`t use auto-configuration, you may use `@EnableMongration`
to configure Mongration.
### @EnableMongoRepositories
If you use `@EnableMongoRepositories` to activate Mongo Data instead of Auto configuration, you will need to make changes in
configuration. To avoid unexpected behavior use annotation `@EnableMongration` along with `@EnableMongoRepositories`
```java
@Configuration
@EnableMongoRepositories
@EnableMongration
public class MongoRepositoriesConfiguration {
    
}
``` 

### Creating first ChangeSet
After adding a dependency you need to create Changelog class and annotate it with `@ChangeLog`
```java
@ChangeLog
public class Changelog {
    
    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void firstMigration(MongoTemplate template) {
        template.save(new Document("key", "value"), "collection");
    }
}
```
ChangeSets will be executed on application startup.
## When migrations are executed
Mongration provides `MongrationAutoConfiguration`, which configures `Mongration` bean after `MongoAutoConfiguration` and 
before `MongoDataAutoConfiguration`. This means that while migrating, Mongo Data is not yet configured. It 
necessary to avoid unexpected behavior like this:

current document mapping:

```java
@Document
public class Entity {
    
    @Id
    private String id;
}
```
new document mapping:
```java
@Document
public class Entity {

    @Id
    private String id;
    @Indexed(unique = true)
    private String text;
}
```

We need to write a migration to fill `text` field with unique values. When this migration is executed, Mongo Data will not
create a unique index with `text` field yet and migration successfully executed. If Mongo Data create index before migration
we will see DuplicateKeyException on startup.

## Locking
Mongration tries to acquire a lock before start executing migrations. If it could not acquire a lock, it would throw exception and application would not start.
If you have several instances of service, they will not start until one instance acquire a lock and execute migrations.

## @ChangeLog
`@ChangeLog` is a meta-annotation to mark a class as ChangeLog. A class annotated with `@ChangeLog` is regular spring bean.
It allows inject dependencies in these classes (do not inject dependencies that depends on Mongo Data components, because
 they are not loaded when migrations executing).
### Ordering
To order ChangeLog\`s you may annotate it with `@Order`. Changelogs are sort with standard spring Order comparator.
## @ChangeSet
`@ChangeSet` is annotation to mark ChangeLog\`s method as migration. This method should have `MongoTemplate` as an argument.

Example: 

```java
@ChangeLog
public class Changelog {
    
    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template){
        template.save(new Document("index", "1").append("text", "1"), "entity");
        template.save(new Document("index", "2").append("text", "2"), "entity");
        template.save(new Document("index", "3").append("text", "3"), "entity");
    }
}
```

### Transactions

If you [configure](https://docs.mongodb.com/v4.0/tutorial/deploy-replica-set/) your MongoDB to support transactions, you may execute `@ChangeSet` in a transaction. Mongration provide 2 ways
 to use transactions:
 ```java
@ChangeLog
public class Changelog {
   
    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    @Transactional //annotate method with @Transactional
    public void changeset1(MongoTemplate template) {
        template.save(new Document("index", "1").append("text", "1"), "entity");
        template.save(new Document("index", "2").append("text", "2"), "entity");
        template.save(new Document("index", "3").append("text", "3"), "entity");
    }
    
    @ChangeSet(order = 2, id = "change2", author = "Stepan")
    //add TransactionTemplate argument
    public void migration(MongoTemplate template, TransactionTemplate txTemplate){
        template.createCollection("entity");
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                template.save(new Document("index", "1").append("text", "1"), "entity");
                template.save(new Document("index", "2").append("text", "2"), "entity");
                template.save(new Document("index", "3").append("text", "3"), "entity");
            }
        });
    }
}
```
To use native mongodb transactions (`@Transactional`) you will need to provide bean of type `MongoTransactionManager`. Mongration
creates this one if it doesn\`t exist.