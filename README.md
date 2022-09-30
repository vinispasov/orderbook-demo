# orderbook-demo


Overview:

Orderbook-demo is a project, which shows real-time market data updates.

It is based on Java 11 with Spring boot framework.
Orderbook-demo uses WebSockets API, which offering fastest real-time data.
There is a NoSql database - Firebase Firestore.
Also you can take a look on a simple unit test example managed with JUnit and Mockito.


How to use the project:

Orderbook-demo can be started from OrderbookDemoApplication.class, where you can see the run configurations of the App.
The project is based on Controller-Service-Repository pattern and we can "subscribe" for the real-time market data updates through the controller layer, where we have the following endpoint:

GET
http://localhost:8080/subscribe (when the application is running locally)


If all went well, you should receive positive response and the data updates should come on the console log.


If you want to stop the data updates in the console log you should use the following endpoint:


GET
http://localhost:8080/unsubscribe (when the application is running locally)





