![Title](images/title.png)

DeTOnator Business Objects are plain Java Objects using the generic Dao interface for persistence. Thus you can create composite
wrappers to add transactional support. 
* Code is portable with Java SE, Jakarta EE transactions (TomEE), Java SE transactions (Guice, AOP and Atomikos). It should not be
hard to implement on Android as well.
* Bean Validation 2.0 used if DTOs are decorated with Bean Validation 2.0 annotations. An exception is thrown if validation fails
and the calling code can do a rollback implicitly.
* See unit tests for example code.
