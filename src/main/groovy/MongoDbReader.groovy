/**
 * Created by shaopengxiang on 2016/11/18.
 */


import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.sql.Sql
import org.bson.Document

import java.sql.Connection;


class MongoDbReader {

    def cols = ['_id', 'mingcheng', 'zuozhe', 'shipin', 'ticai', 'chaodai', 'guojia', 'fenlei', 'jieduan', 'keben', 'congshu', 'chuchu', 'zhaiyao', 'yuanwen']

    def readData() {

        def db = new GroovyDataLoader().connectToDb()
        MongoCollection<Document> poemCollection = db.getCollection("poem");

        def sql = Sql.newInstance(
                "jdbc:sqlite:poem.db",
                "shaopx",
                "shaopx2016",
                "org.sqlite.JDBC")

        def sql_id_list = "select * from poem where _id"
        def builder = new JsonBuilder()


        BasicDBObject query = new BasicDBObject();


        def count = 0
        sql.eachRow(sql_id_list) {
            row ->
                query.put("_id", row["_id"]);
                def find = poemCollection.find(query)

                if (find.size() == 0) {
                    Document document = new Document();
                    document.put("pid", "" + row["_id"]);
                    cols.each { col ->
                        if (col != '_id')
                            document.put("$col", "" + row[col]);
                    }

                    poemCollection.insertOne(document);
                } else {
                    //poemCollection.up
                }
                // 构建json格式的poem
//                builder.poem {
//                    cols.each { col->
//                        "$col" ""+row[col]
//                    }
////                    'poem' row['poem']
//                }
//                def finalData = JsonOutput.prettyPrint(builder.toString())
                //println finalData

                println '' + count++
        }
    }

    static def main(args) {
        new MongoDbReader().readData()
    }
}
