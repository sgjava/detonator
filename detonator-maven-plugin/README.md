![Title](images/title.png)

While you can certainly use the code generation directly in your project it makes sense to generate common artifacts in Maven for
your Maven based projects. The [DAO project](https://github.com/sgjava/detonator/tree/master/dao) uses this plugin to generate
artifacts in the generated-test-sources and generated-test-resources directories for the unit tests. For instance to generate DTOs,
IDs and SQL properties files you can use something like:
'''
<plugin>
   <groupId>com.codeferm</groupId>
   <artifactId>detonator-maven-plugin</artifactId>
   <version>1.0.0-SNAPSHOT</version>
   <configuration>
       <dbDriver>org.h2.Driver</dbDriver>
       <dbUser>sa</dbUser>
       <dbPassword></dbPassword>
       <dbUrl>jdbc:h2:~/test</dbUrl>
       <dbPoolSize>5</dbPoolSize>
       <templatesDir>${project.basedir}/../dto/src/main/resources/templates</templatesDir>
       <dtoTemplate>dto.ftl</dtoTemplate>
       <idTemplate>id.ftl</idTemplate>
       <sqlTemplate>sql.ftl</sqlTemplate>
       <packageName>com.codeferm.dto</packageName>
       <sqlMap>
           <Orders>select * from orders</Orders>
           <OrderItems>select * from order_items</OrderItems>
           <RegionscCountries>select * from regions r, countries c where r.region_id = c.region_id</RegionscCountries>
       </sqlMap>                    
   </configuration>
   <executions>
       <execution>
           <phase>generate-test-sources</phase>
              <goals>
                   <goal>testGenerate</goal>
              </goals>
       </execution>
   </executions>                
</plugin>'''
