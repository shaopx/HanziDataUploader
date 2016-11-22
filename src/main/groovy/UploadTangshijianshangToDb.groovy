import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.util.JSON
import org.bson.Document

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by SHAOPENGXIANG on 2016/11/22.
 */
class UploadTangshijianshangToDb {
    MongoCollection<Document> tsjsCollection;

    def rootPath ="C:\\Dev\\data\\poem\\tagnshijianshang\\root\\"
    def dir = new File(rootPath)

    void upload(){
        def db = new GroovyDataLoader().connectToDb()
        tsjsCollection = db.getCollection("tsjs",BasicDBObject.class);

        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                insertOneFile(file)
            }
        }


    }

    def insertOneFile(File file){
        def strJson = file.text
        BasicDBObject document = (BasicDBObject) JSON.parse(strJson);
        def poem = document.get("poem");
        BasicDBObject tsjsDocument = new BasicDBObject();
        tsjsDocument.put("n", poem.get("n"));
        tsjsDocument.put("a", poem.get("a"));
        tsjsDocument.put("c", poem.get("c"));
        tsjsDocument.put("sx", poem.get("sx"));
        tsjsDocument.put("sxa", poem.get("sxa"));
        tsjsDocument.put("r", poem.get("r"));
        tsjsCollection.insertOne(tsjsDocument);
    }

    static void main(args){
        new UploadTangshijianshangToDb().upload()
    }
}
