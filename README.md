![Title](images/title.png)

Everything should be made as simple as possible, but not simpler - Albert Einstein

DeTOnator is a set of projects based on an idea a colleague and I came up with about ten years ago. At that time we were generating
code for Spring, JdbcTemplate and JSF with only one set of validation rules propagated to UI and DAOs. At the time there was FireStorm
DAO, but it was commercial and now it's a dead project from what I can tell. There are other products/projects like JOOQ out there
that do code generation, but typically you end up with one DAO per table. DeTOnator uses a truly generic DAO interface and implementation.
* [DeTOnator DTO](https://github.com/sgjava/detonator/tree/master/dto) generates Java DTOs, IDs and SQL, but could be use to generate other
artifact types and languages.
* [DeTOnator Maven Plugin](https://github.com/sgjava/detonator/tree/master/detonator-maven-plugin) leverages DeTOnator DTO to add code
generation to your Maven based projects.
* [DeTOnator DAO](https://github.com/sgjava/detonator/tree/master/dao) is a generic DAO layer.
* Based on OpenJDK 11 LTS.
