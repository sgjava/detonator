![Title](images/title.png)

DeTOnator TomEE shows you how to easily integrate DeTOnator DAO into a Jakarta EE application without using JPA (PersistenceContext or EntityManager).
Minimal additional classes are needed to wrap business objects and message driven beans (MDBs) are used to replace Java SE queues.
* A producer class is used as a DAO factory that will return the proper DAO type using the container's DataSource and injecting
the DAO into your Java EE beans.
* A producer class is used to allow injection of Business Objects into EJB wrapper class.
* An XADataSource is used along with TomEE's transaction manager to support implicit transactions. Use method or class level
jakarta.transaction.Transactional annotation to set scope.
* EjbContainer uses log4j2 like the test and application code.
* See unit tests for example code.
