![Title](images/title.png)

DeTOnator DAO is a truly generic DAO framework. Unfortunately I continue to see [blog posts](https://www.baeldung.com/java-dao-pattern)
describing the DAO pattern where you create a DAO for each entity based on an interface. I have been using a single DAO for many years
now inspired by [Don't repeat the DAO!](https://www.ibm.com/developerworks/library/j-genericdao/index.html). DeTOnator DAO goes back to
basics without relying on DI, Spring or any other frameworks other than the persistence implementation. This however doesn't preclude
you from using DI, AOP, etc. to declaratively add transactions, interceptors, etc. You may also want to inject a particular
implementation in your business objects for a key value store instead of a RDBMS for instance.
* Use the [DeTOnator Maven Plugin](https://github.com/sgjava/detonator/tree/master/detonator-maven-plugin) to generate DTOs, IDs and SQL
for your project. DeTOnator DAO uses this plugin to generate entities and SQL for the unit tests.
* Query result mapping is based on database field name being snakeCase (FIELD_NAME) and member variables being lower camelCase
(fieldName). Some implementations such as
DbUtils [GenerousBeanProcessor](https://commons.apache.org/proper/commons-dbutils/apidocs/org/apache/commons/dbutils/GenerousBeanProcessor.html)
may allow a less rigid set of mapping rules. You can always use a field alias to fix column names that do not map properly.
* Parameter mapping is ordinal based on a bean's accessor methods. DeTOnator DTO generates DTOs, IDs and SQL in the proper order
(alphabetical) for mapping to work.
* Queries are stored by name in a Properties object making it easy to modify the generated SQL or add named queries for instance.
* DeTOnator DTO allows optional Java type mapping. This means your DAO implementations must support mapping BigDecimal to Integer for
instance. GenerousBeanProcessor in DbUtils handles this mapping automatically.
* CQRS is supported out of the box since composite (i.e. more than one table in select) SQL is supported. You can generate a DTO that
handles composite SQL (query) and use single table SQL (command) to generate DTOs and SQL for DML operations. This is also useful
for legacy databases that were not forward engineered from an ORM type tool.
* See unit tests for example code.
