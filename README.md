![Title](images/title.png)

Everything should be made as simple as possible, but not simpler - Albert Einstein

DeTOnator is an easy to use code generator that explodes database schema into DTOs, CRUD operations, SQL statements, etc. Being
template driven you can easily modify the templates to generate code for a particular framework or even another language
besides Java. Since no IDE plugins are required it's ideal for headless environments like CI/CD, build servers, etc.

DeTOnator reads your database schema to build artifacts, so it can be used to detect changes in the schema that breaks your
code. By using DTOs and Java's statically-typed nature you will see when field names are removed or changed. Adding nullable
fields usually will not break your code. Typically with most Java data layers or dynamic languages your code will blow up at
run time. Just make sure to always use a field list for select instead of select *. select * is handy for generating DML SQL,
but as the table schema is altered select * will pick up all changes.

CQRS is supported out of the box since composite (i.e. more than one table in select) SQL is supported. You can generate a DTO that
handles composite SQL (query) and use single table SQL (command) to generate DTOs and SQL for DML operations. This is also useful
for legacy databases that were not forward engineered from an ORM type tool.

Based on OpenJDK 11 LTS, but can generate any target by modifying the templates. Currently the DTOs and PKOs support Java 7+.

Currently Java DTOs and DML SQL are working against H2 and Oracle databases. DeTOnator is in the very early stages of development,
but I wanted to put it out there for others to look at. Feel free to make suggestions in the issues section of this project.