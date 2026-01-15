# My Book Store - Demo 

This project show how to use DynamiaTools framework. This demo web application is built using Spring boot and DynamiaTools 

MyLibrary is a web application that help users to organize and sell books. Books can be filtered by category, ISBN code, name and authors. Also you can create an invoice to sell books to customers.

## Entities
1. **Books**: data about books like name, synopsis, ISBN, sell price, cover
2. **Category**: book's category
3. **Author**: first name, last name, email, photo and bio
4. **Invoice**: Invoice and invoice details
5. **Customer**: People how buy books

## Database

This demo use H2 database in memory. If you want to use another follow next steps:

- Edit ``resources/application.properties``
- Change datasource config
- Add database driver dependency
- Run

## Build
This is a standard maven project, just execute

```
mvn clean install
```

Also you can import it in your favorite Java IDE like Intellij, Eclipse or Netbeans.

## Run

### Maven

```
mvn spring-boot:run
```
### Terminal

1. Open a terminal 
2. Go to `demo/target/` folder
3. Execute `java -jar mybookstore.jar`
4. Open your web browser at [http://localhost:8484](http://localhost:8484)
5. Enjoy