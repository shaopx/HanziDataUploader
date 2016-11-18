import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase

/**
 * Created by shaopengxiang on 2016/11/18.
 */
class MongoDbManager {

//    MongoDatabase getMongoDb(){
//        MongoClient mongoClient = getMongoClient();
//
//        MongoDatabase db = mongoClient.getDatabase(dbName);
//    }
//
//    private MongoClient getMongoClient() {
//        final List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
//        credentialsList.add(MongoCredential.createCredential(db.host, dbName, dbPwd.toCharArray()));
//        MongoClient mongoClient = new MongoClient(new ServerAddress(dbHost, dbPort.toInteger()), credentialsList);
//        return mongoClient;
//    }
}
