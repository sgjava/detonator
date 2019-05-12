![Title](images/title.png)

While you can certainly use DeTOnator DTO directly in your projects it makes sense to generate common artifacts in Maven for your
Maven based projects. DeTOnator DAO uses this plugin to generate artifacts in the generated-test-sources and generated-test-resources
directories for the unit tests. For instance to generate DTOs, IDs and SQL properties files you can use something like
[this](https://github.com/sgjava/detonator/blob/3304a7b407646b8e77298596f48e6001136d6dd8/dao/pom.xml#L21).
* You can use a SQL MAP for standard and custom composite code generation.
* You can use entire or partial database schema for code generation.
* Database pooling and multi threading are used for maximum performance. This allows you to efficiently generate code for databases
with hundreds or thousands of tables.


