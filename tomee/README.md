![Title](images/title.png)

DeTOnator TomEE shows you how to easily integrate DeTOnator DAO into a Jakarta EE application without all the complications and
overhead of JPA. A simple producer class is used as a DAO factory that will return the proper DAO type using the container's
DataSource and injecting the DAO into your business object bean.
* An XADataSource is used along with TomEE's transaction manager to support implicit transactions using method or class level using normal Jakarta EE annotations. 
* See unit tests for example code.
