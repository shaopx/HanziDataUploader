import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.util.JSON
import db.GroovyDataLoader
import org.bson.Document

/**
 * Created by SHAOPENGXIANG on 2016/11/22.
 */
class UploadTangshijianshangToDb {
    MongoCollection<Document> tsjsCollection;

    def rootPath ="C:\\Dev\\data\\poem\\tagnshijianshang\\root\\"
    def dir = new File(rootPath)
    def dbLoader =GroovyDataLoader.instance

    void upload(){
        def mongoDb = dbLoader.getOnlineDb()
        tsjsCollection = mongoDb.getCollection("tsjs");

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
        Document tsjsDocument = new Document();
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
