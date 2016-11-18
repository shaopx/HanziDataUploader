import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import groovy.sql.Sql
import org.bson.Document

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by shaopengxiang on 2016/11/18.
 */
class PutAllTogether {
    def dbLocation = 'C:\\Dev\\gushiwen\\dbs\\'
    def cols = ['_id', 'mingcheng', 'zuozhe', 'shipin', 'ticai', 'chaodai', 'guojia', 'fenlei', 'jieduan', 'keben', 'congshu', 'chuchu', 'zhaiyao', 'yuanwen']

    def perform() {
        def dir = new File(dbLocation)
//        if (dir.isDirectory()) {
//            dir.eachFileRecurse { file ->
//                Files.copy(Paths.get(file.getPath()), Paths.get("" + file.getName()))
//            }
//        }


        def db = new GroovyDataLoader().connectToDb()
        MongoCollection<Document> poemCollection = db.getCollection("poem");


        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")
        def sql_shangxi = Sql.newInstance("jdbc:sqlite:poem_shangxi.db", "", "", "org.sqlite.JDBC")
        def sql_yiwen = Sql.newInstance("jdbc:sqlite:poem_yiwen.db", "", "", "org.sqlite.JDBC")
        def sql_zhujie = Sql.newInstance("jdbc:sqlite:poem_zhujie.db", "", "", "org.sqlite.JDBC")
        def sql_zuozhe = Sql.newInstance("jdbc:sqlite:poem_zuozhe.db", "", "", "org.sqlite.JDBC")

        def count = 0
        BasicDBObject query = new BasicDBObject();
        def sql_id_list = "select * from poem where _id<100"
        def getValue{ tableName, id->
            "select $tableName from $tableName where _id=" +id
            sql_shangxi.query(""){

            }
        }
        sql_poem.eachRow(sql_id_list) {
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

                println '' + count++
        }

        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                new File("./" + file.getName()).delete()
            }
        }
    }

    static def main(args) {
        new PutAllTogether().perform()
    }
}
