/**
 * Created by SHAOPENGXIANG on 2016/11/3.
 */
//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
import com.mongodb.*;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import groovyx.net.http.HTTPBuilder

//import groovyx.net.http.ContentType // this doesn't import ContentType
//import groovyx.net.http.Method // this doesn't import Method
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator

// ContentType static import
import static groovyx.net.http.ContentType.*

// Method static import
import static groovyx.net.http.Method.*

import java.util.ArrayList;
import java.util.List;

//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
class GroovyDataLoader {
    def dbHost, dbPort, dbName, dbUser, dbPwd;

    public GroovyDataLoader() {
        init();
    }

    def connectToDb() {
        MongoClient mongoClient = getMongoClient();

        MongoDatabase db = mongoClient.getDatabase(dbName);
        ListCollectionsIterable<Document> documents = db.listCollections();
        System.out.println("db.name:" + db.getName());
        System.out.println("documents:" + documents.toString());
//        db.createCollection("testcollections");
        MongoCollection<Document> testcollections = db.getCollection("testcollections");
    }

    private init() {
        new File("application.properties").eachLine { line ->
            println "Line: ${line}"
            if (line.startsWith("db.host:")) {
                dbHost = line.substring("db.host:".length());
            } else if (line.startsWith("db.port:")) {
                dbPort = line.substring("db.port:".length());
            } else if (line.startsWith("db.name:")) {
                dbName = line.substring("db.name:".length());
            } else if (line.startsWith("db.user:")) {
                dbUser = line.substring("db.user:".length());
            } else if (line.startsWith("db.password:")) {
                dbPwd = line.substring("db.password:".length());
            }
        }

        println "dbHost:" + dbHost;
        println "dbPort:" + dbPort;
        println "dbName:" + dbName;
        println "dbUser:" + dbUser;
        println "dbPwd:" + dbPwd;
    }

    private void loadOnlineData() {

//        def file = new File("hanzi_1.txt")
//
//        if (file.exists())
//            file.delete()
//        def printWriter = file.newPrintWriter() //

//        def http = new HTTPBuilder()
//        http.request('http://52.27.4.79:8080', GET, TEXT) { req ->
//            uri.path = '/docs/hanzi_1_20161103.txt'
//            headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
//            headers.Accept = 'application/json'
//
//            response.success = { resp, reader ->
////                assert resp.statusLine.statusCode == 200
//                println "Got response: ${resp.statusLine}"
//                println "Content-Type: ${resp.headers.'Content-Type'}"
//                println reader.text
//                if(reader.text!=null){
//                    def trimText = reader.text.trim();
//                    //printWriter.println(trimText);
//                }
//            }
//
//            response.'404' = {
//                println 'Not found'
//            }
//        }
//        printWriter.flush();
//        printWriter.close();


        def text = new URL("http://52.27.4.79:8080/docs/hanzi_1_20161103.txt")
                .getText(connectTimeout: 5000,
                readTimeout: 10000,
                useCaches: true,
                allowUserInteraction: false,
                requestProperties: ['Connection': 'close'])
        println text;

        def split = text.split("\r\n");
//        for(0)
    }

    private MongoClient getMongoClient() {
        final List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
        credentialsList.add(MongoCredential.createCredential(dbUser, dbName, dbPwd.toCharArray()));
        MongoClient mongoClient = new MongoClient(new ServerAddress(dbHost, dbPort.toInteger()), credentialsList);
        return mongoClient;
    }

    public static void main(String[] args) throws Exception {

        GroovyDataLoader loader = new GroovyDataLoader();
        loader.connectToDb();

        loader.loadOnlineData();
    }
}
