![Title](images/title.png)

DeTOnator Guice allows you to use JTA transactions in Java SE without the overhead of Spring or JEE container.
Minimal additional classes are needed to wrap business objects and add transactional support.
* Method level transactions can be added with a simple @Transaction annotation. Transactions can cross thread boundaries and
multiple connections.
* Narayana used for JTA.
* See unit tests for example code.
