![Title](images/title.png)

DeTOnator TomEE shows you how to easily integrate DeTOnator DAO into a Jakarta EE application without all the complications and
overhead of JPA. A simple producer class is used as a DAO factory that will return the proper DAO type using the container's
DataSource and injecting the DAO into your business object bean.
* Simple producer class acts as a DAO factory, so your bean only needs to do a @Inject to get a DAO.
* An XADataSource is used along with TomEE's transaction manager to support implicit transactions. Use method or class level TransactionAttribute annotation just
like JPA.
* See unit tests for example code.
* EjbContainer uses log4j2 like the test and application code.
