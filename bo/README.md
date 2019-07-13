![Title](images/title.png)

DeTOnator Business Objects are plain Java Objects using the generic Dao interface for persistence. Thus you can create composite
wrappers to add transactional support. One of the corner cases is using a RDBMS or Map to update inventory. Since everything should
be built thread safe you have to consider concurrent database updates. If multiple threads read the same value then the product
quantity will get out of sync. The simple way to solve this is to use a queue and let it handle the single threading. I've implemented
this with Java SE, Java EE and Guice/AOP. Keeping the business logic separated from the underlying technology (JMS, MDB, Guice, etc.)
is easy with simple wrapper classes.

A future version will include concurrent inventory management by using atomics. I'm already doing this with MapDB if the PK is a
single field Long. Generated keys must be thread safe and atomics handle this. I envision loading all inventory quantities into
atomics and periodically update the persistence store. This would allow a much more scalable solution. 
* Code is portable with Java SE, Jakarta EE transactions (TomEE), Java SE transactions (Guice, AOP and Atomikos). It should not be
hard to implement on Android as well.
* Bean Validation 2.0 used if DTOs are decorated with Bean Validation 2.0 annotations. An exception is thrown if validation fails
and the calling code can do a rollback implicitly.
* Orders are created using a queue to adjust inventory in thread safe way.
* See unit tests for example code.
