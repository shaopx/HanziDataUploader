import com.mongodb.*;
        import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
        import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SHAOPENGXIANG on 2016/11/3.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        final List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
        credentialsList.add(MongoCredential.createCredential("shaopx", "mydb", "shaopx2016".toCharArray()));
        MongoClient mongoClient = new MongoClient(new ServerAddress("52.27.4.79", 27017), credentialsList);
        MongoDatabase db = mongoClient.getDatabase("mydb");
        ListCollectionsIterable<Document> documents = db.listCollections();
        System.out.println("documents:" + documents.toString());
        db.createCollection("testcollections");
        MongoCollection<Document> testcollections = db.getCollection("testcollections");

    }
}
