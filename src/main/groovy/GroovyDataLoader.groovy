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

import java.nio.file.Files
import java.nio.file.Paths

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

@Singleton
class GroovyDataLoader {
    static def dbHost, dbPort, dbName, dbUser, dbPwd;

    static {
        init()
    }

    //def dbLocation = 'C:\\Dev\\gushiwen\\dbs\\'
    def dbLocation = 'D:\\data\\kkpoem\\dict\\'
    def dir = new File(dbLocation)

    def getOnlineDb() {

        MongoClient mongoClient = getMongoClient();

        MongoDatabase db = mongoClient.getDatabase(dbName);

        return db;
    }

    void copyDbs() {

        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                if (!(new File("" + file.getName()).exists()))
                    Files.copy(Paths.get(file.getPath()), Paths.get("" + file.getName()))
            }
        }
    }

    void clearDbs() {
        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                new File("./" + file.getName()).delete()
            }
        }
    }

    static void init() {
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


    private MongoClient getMongoClient() {
        final List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
        credentialsList.add(MongoCredential.createCredential(dbUser, dbName, dbPwd.toCharArray()));
        MongoClient mongoClient = new MongoClient(new ServerAddress(dbHost, dbPort.toInteger()), credentialsList);
        return mongoClient;
    }

    public Map<String, String> wordMap = new HashMap<>();

    public void handleLocalData() {

        new File("hanzi_1.txt").eachLine { line ->

            if (line == null || line.trim().length() == 0) {
                //do nothing
            } else {
                //println "Line: ${line}"
                handleOneLine(line);
            }
        }
    }

    protected void handleOneLine(String line) {

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

        MongoDatabase db = connectToDb();
        MongoCollection<Document> collections = db.getCollection("testcollections");


        int totalWordCount = 0;
        def set = wordMap.keySet();
        for (key in set) {
            String words = wordMap.get(key)
            totalWordCount += words.length();

            List<String> wordList = new ArrayList<>()
            for (int j = 0; j < words.length(); j++) {
                wordList.add("" + words.charAt(j));
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
