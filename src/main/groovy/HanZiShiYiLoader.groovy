import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import db.GroovyDataLoader
import org.bson.Document

import static groovyx.net.http.ContentType.URLENC
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.JSON

/**
 * Created by shaopengxiang on 2016/11/9.
 */
class HanZiShiYiLoader extends GroovyDataLoader {


    public void loadShiYi() {
        //http://dict-mobile.iciba.com/interface/index.php?c=word&list=100,24,8,21,22,10,9,15,2,5,14,4,6,7&client=1&timestamp=1478683173&sign=948cfe904719a74b&uuid=7932d1177242405a913685ef25023fb3&sv=android6.0&v=8.4.6&uid=word=%25E5%258E%25BB
        MongoDatabase db = getOnlineDb();
        MongoCollection<Document> collections = db.getCollection("shiyi");

        def set = wordMap.keySet();
        for (key in set) {
            String words = wordMap.get(key)


            for (int i = 0; i < words.length(); i++) {
                char ch = words.charAt(i);
                String word = "" + ch;

                insertOneWord(collections, key, word);
            }


        }



    }


    void insertOneWord(MongoCollection<Document> shiyiCollection, String fayin, String word) {
        println "insert word:" + word;
        BasicDBObject query = new BasicDBObject();
        query.put("word", word);
//            query.put("", )
        def find = shiyiCollection.find(query)

        if (find.size() == 0) {
            println word + " not exist!"

            Document document = new Document();
            document.put("word", word);
            document.put("yin", fayin);

            def http = new HTTPBuilder('http://dict-mobile.iciba.com/')
            http.request( POST , JSON ) {
                uri.path = '/interface/index.php'
                uri.query = [c:'word', list:'100,24,8,21,22,10,9,15,2,5,14,4,6,7', client:'1', timestamp:'1478683173', sign:'948cfe904719a74b']
                requestContentType = URLENC
                body =  [word: word]

                response.success = { resp, json ->
                    println "POST response status: ${json.message.chinese}"
                    document.put("data", json.message);
                    shiyiCollection.insertOne(document);
                }
            }
        } else {
            println word + " exist!"
        }
    }

    public static void main(String[] args) throws Exception {
        HanZiShiYiLoader loader = new HanZiShiYiLoader();
        loader.handleLocalData();
        loader.loadShiYi();
    }
}
