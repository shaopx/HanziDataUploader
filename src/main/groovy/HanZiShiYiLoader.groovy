import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.bson.Document
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC

/**
 * Created by shaopengxiang on 2016/11/9.
 */
class HanZiShiYiLoader extends GroovyDataLoader {


    public void loadShiYi() {
        //http://dict-mobile.iciba.com/interface/index.php?c=word&list=100,24,8,21,22,10,9,15,2,5,14,4,6,7&client=1&timestamp=1478683173&sign=948cfe904719a74b&uuid=7932d1177242405a913685ef25023fb3&sv=android6.0&v=8.4.6&uid=word=%25E5%258E%25BB
        MongoDatabase db = connectToDb();
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

//            def http = new HTTPBuilder( 'http://dict-mobile.iciba.com' )
//            def postBody = [word: word] // will be url-encoded
//
//            http.post( path: '/interface/index.php?c=word&list=100,24,8,21,22,10,9,15,2,5,14,4,6,7&client=1&timestamp=1478683173&sign=948cfe904719a74b&uuid=7932d1177242405a913685ef25023fb3&sv=android6.0&v=8.4.6&uid=', body: postBody) { resp ->
//
//                println "POST Success: ${resp.statusLine}"
//            }

//            def http = new HTTPBuilder( 'http://dict-mobile.iciba.com' )
//            http.request( Method.POST ) {
//                uri.path = '/interface/index.php?c=word&list=100,24,8,21,22,10,9,15,2,5,14,4,6,7&client=1&timestamp=1478683173&sign=948cfe904719a74b&uuid=7932d1177242405a913685ef25023fb3&sv=android6.0&v=8.4.6&uid='
//                requestContentType = ContentType.TEXT
//                body =  [word: word]
//
//                response.success = { resp ->
//                    println "POST response status: ${resp.statusLine}"
//                }
//            }
            def http = new HTTPBuilder('http://127.0.0.1:8080/')
            def postBody = [word: 'aaaa'] // will be url-encoded

            http.post(path: '/postword', body: postBody,
                    requestContentType: URLENC) { resp ->

                println "POST Success: ${resp.statusLine}"

            }

//            def ret = null
//            def http = new HTTPBuilder('http://dict-mobile.iciba.com')
//
//            // perform a POST request, expecting TEXT response
//            http.request('http://dict-mobile.iciba.com', Method.POST, ContentType.TEXT) { req ->
//                uri.path = '/interface/index.php?c=word&list=100,24,8,21,22,10,9,15,2,5,14,4,6,7&client=1&timestamp=1478683173&sign=948cfe904719a74b&uuid=7932d1177242405a913685ef25023fb3&sv=android6.0&v=8.4.6&uid='
//                uri.query = [word: word]
//                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
//                headers.Accept = 'application/json'
//
//                // response handler for a success response code
//                response.success = { resp, reader ->
//                    println "response status: ${resp.statusLine}"
//                }
//
//                response.'404' = {
//                    println 'Not found'
//                }
//            }

            //document.put("data", data);

            //shiyiCollection.insertOne(document);
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
