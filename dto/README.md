![Title](images/title.png)

DeTOnator DTO is an easy to use code generator that explodes database schema into DTOs, IDs, SQL, etc. Being template driven you can
modify or create new templates to generate code for a particular framework or even another language besides Java. 
* DeTOnator DTO reads your database schema to build artifacts, so it can be used to detect changes in the schema that breaks your
code. By using DTOs and Java's type safety you will see when field names are removed or changed. Adding nullable fields usually will
not break your code. Typically with most Java data layers or dynamic languages your code will blow up at run time. Just make sure to
always use a field list for select instead of select *. select * is handy for generating DML SQL, but as the table schema is altered
select * will pick up all the changes.
* Most JDBC drivers should work, but I've only tested H2 and Oracle 18c at this point. Create an issue if you run into any JDBC driver
issues.
* Minimal dependencies are required reducing the transitive dependencies included in your project.
* Output is in the form of a Java Writer class thus making it easy to go between String, file, pipe, etc.
* Optional Java type mapping allows BigDecimal with a scale of zero to be mapped to integer types based on precision. Other mappings
could be added as required.
* FreeMarker Java Template Engine is used to render the output.
* See unit tests for example code.

