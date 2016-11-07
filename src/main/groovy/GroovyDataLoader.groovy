/**
 * Created by SHAOPENGXIANG on 2016/11/3.
 */
//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
import com.mongodb.*
import com.mongodb.client.FindIterable;
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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

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

        return testcollections;
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

        def file = new File("hanzi_1.txt")

        if (file.exists())
            file.delete()
        def printWriter = file.newPrintWriter() //

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


        def text = new URL("http://52.27.4.79:8080/docs/hanzi_1_20161103.txt")
                .getText(connectTimeout: 5000,
                readTimeout: 10000,
                useCaches: true,
                allowUserInteraction: false,
                requestProperties: ['Connection': 'close'])
        //println text;
        printWriter.println(text);
        printWriter.flush();
        printWriter.close();

        def split = text.split("\r\n ");

        for (item in split) {
            println item
        }
        println "text.length:" + text.length();
        println "split.length:" + split.length;
    }

    private MongoClient getMongoClient() {
        final List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
        credentialsList.add(MongoCredential.createCredential(dbUser, dbName, dbPwd.toCharArray()));
        MongoClient mongoClient = new MongoClient(new ServerAddress(dbHost, dbPort.toInteger()), credentialsList);
        return mongoClient;
    }

    Map<String, String> wordMap = new HashMap<>();

    private void handleLocalData() {

        new File("hanzi_1.txt").eachLine { line ->

            if (line == null || line.trim().length() == 0) {
                //do nothing
            } else {
                //println "Line: ${line}"
                handleOneLine(line);
            }
        }
    }

    private void handleOneLine(String line) {

        int index = 1;
        for (int i = 0; i < line.length() - 1; i++) {
            char at = line.charAt(i);
            if (at < 'A' || at > 'z') {
                index = i;
                break;
            }
        }
        String key = line.substring(0, index);
        String words = line.substring(index)
        println key + ":" + words;
        wordMap.put(key, words);
    }

    void buildJson() {

        MongoCollection<Document> collections = connectToDb();


        int totalWordCount = 0;
        def set = wordMap.keySet();
        for (key in set) {
            String words = wordMap.get(key)
            totalWordCount += words.length();

            List<String> wordList = new ArrayList<>()
            for(int j=0;j<words.length();j++){
                wordList.add(""+words.charAt(j));
            }


            BasicDBObject query = new BasicDBObject();
            query.put("key", key);
            def find = collections.find(query)

            if (find.size() == 0) {
                println key + " not exist!"

                Document document = new Document();
                document.put("key", key);
                document.put("words", wordList);


                collections.insertOne(document);
            } else {
                println key + " exist!"
            }
        }

        println "totalwordcount:" + totalWordCount;


    }

    public static void main(String[] args) throws Exception {

        GroovyDataLoader loader = new GroovyDataLoader();
//        loader.connectToDb();

        //loader.loadOnlineData();

        loader.handleLocalData();

        loader.buildJson();
    }


}
