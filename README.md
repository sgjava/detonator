![Title](images/title.png)

Everything should be made as simple as possible, but not simpler - Albert Einstein

DeTOnator is an easy to use code generator that explodes database schema into DTOs, IDs, SQL, etc. Being template driven you can
easily modify the templates to generate code for a particular framework or even another language besides Java. Since no IDE plugins
or GUIs are required it's ideal for headless environments like CI/CD, build servers, etc. About ten years ago a colleague and myself
came up with something similar, but it was built for Spring and JdbcTamplate. At the time there was FireStorm DAO, but it was
commercial and now it's a dead project from what I can tell. There are other products/projects out there that do code generation,
but typically you end up with on DAO per table. DeTOnator uses a truly generic DAO interface and implementation.
* DeTOnator reads your database schema to build artifacts, so it can be used to detect changes in the schema that breaks your
code. By using DTOs and Java's statically-typed nature you will see when field names are removed or changed. Adding nullable
fields usually will not break your code. Typically with most Java data layers or dynamic languages your code will blow up at
run time. Just make sure to always use a field list for select instead of select *. select * is handy for generating DML SQL,
but as the table schema is altered select * will pick up all changes.
* A [Maven plugin](https://github.com/sgjava/detonator/tree/master/detonator-maven-plugin) is provided to generate DTOs, IDs and SQL
in the generated-sources or generated-test-sources directory. This
provides a simple way to generate code for your project.
* A [DAO project](https://github.com/sgjava/detonator/tree/master/dao) is included to to create a truly generic DAO layer. You only
need a single DAO implementation instead of one for each table as is common with JPA, Hibernate, etc. Custom queries are a snap to
add as well as optimized batch operations.
* CQRS is supported out of the box since composite (i.e. more than one table in select) SQL is supported. You can generate a DTO that
handles composite SQL (query) and use single table SQL (command) to generate DTOs and SQL for DML operations. This is also useful
for legacy databases that were not forward engineered from an ORM type tool.
* Based on OpenJDK 11 LTS, but can generate any target by modifying the templates. Currently the DTOs and IDs support Java 7+, H2 and
Oracle databases.
