![Title](images/title.png)

DeTOnator DTO is an easy to use code generator that explodes database schema into DTOs, IDs, SQL, etc. Being template driven you can
easily modify the templates to generate code for a particular framework or even another language besides Java. Since no IDE plugins
or GUIs are required it's ideal for headless environments like CI/CD, build servers, etc.
* DeTOnator reads your database schema to build artifacts, so it can be used to detect changes in the schema that breaks your
code. By using DTOs and Java's statically-typed nature you will see when field names are removed or changed. Adding nullable
fields usually will not break your code. Typically with most Java data layers or dynamic languages your code will blow up at
run time. Just make sure to always use a field list for select instead of select *. select * is handy for generating DML SQL,
but as the table schema is altered select * will pick up all changes.
* Minimal dependencies are required, so you should not end up with a bunch of conflicts or exclusions adding DTO generation to your
own projects.
* Output is in the form of a Java Writer class thus making it easy to go between String, file, pipe, etc.
* FreeMarker Java Template Engine is used to render the output.
