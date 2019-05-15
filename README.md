![Title](images/title.png)

Everything should be made as simple as possible, but not simpler - Albert Einstein

DeTOnator is a set of projects based on an idea a colleague and I came up with about ten years ago. At that time we were generating
code for Spring, JdbcTemplate and JSF with only one set of validation rules propagated to UI and DAOs. There used to be FireStorm
DAO, but it was commercial and now it's a dead project from what I can tell. There are other products/projects like JOOQ out there
that do code generation, but typically you end up with one DAO per table. Even with generated code this will pollute your business
objects with unnecessary classes. DeTOnator uses a generic DAO interface and implementations. A Java SE based JTA is provided with
annotations, Guice and AOP magic.
* [DeTOnator DTO](https://github.com/sgjava/detonator/tree/master/dto) generates Java DTOs, keys and SQL, but could be use to generate
other artifact types and languages.
* [DeTOnator Maven Plugin](https://github.com/sgjava/detonator/tree/master/detonator-maven-plugin) leverages DeTOnator DTO to add
high performance code generation to your Maven based projects.
* [DeTOnator DAO](https://github.com/sgjava/detonator/tree/master/dao) is a generic Java SE DAO layer.
* [DeTOnator Guice](https://github.com/sgjava/detonator/tree/master/guice) JTA transactions in Java SE.
* Based on OpenJDK 11 LTS.
