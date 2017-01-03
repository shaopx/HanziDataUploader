package zuozhe

import com.mongodb.client.MongoCollection
import db.GroovyDataLoader
import groovy.sql.Sql
import org.bson.Document
import util.Utils

/**
 * Created by SHAOPENGXIANG on 2017/1/3.
 */
class ZuozheUploader {

    def dbLoader = GroovyDataLoader.instance
    def MongoCollection<Document> zuozheCl = null


    void perform(){
        dbLoader.copyDbs()



        def mongoDb = dbLoader.getOnlineDb()
        zuozheCl = mongoDb.getCollection("zuozhe");

        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")

        def sql_id_list = "select * from author where _id >0 and _id<=300000 "

        sql_poem.eachRow(sql_id_list) { row ->
            def pauthor = row["xingming"]
            def pjieshao = row["jieshao"]
            def pid = row["_id"]

            def time = Utils.getCurentTime('yyyy-MM-dd HH:mm:ss')

            def datamap = [:]
            datamap['zhuozhe_id'] = pid
            datamap['zuozhe'] = pauthor
            datamap['jieshao'] = pjieshao
            datamap['date'] = time

            saveWord(datamap)
        }

    }

    void saveWord(datamap) {
        Document document = new Document();

        datamap.each() {
            document.append(it.key, it.value.toString());
        }
        zuozheCl.insertOne(document);
    }

    static void main(args) {
        new ZuozheUploader().perform()
    }

}
