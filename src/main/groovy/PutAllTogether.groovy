import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import groovy.sql.Sql
import org.bson.Document

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by shaopengxiang on 2016/11/18.
 */
class PutAllTogether {
    //def dbLocation = 'C:\\Dev\\gushiwen\\dbs\\'
    def dbLocation = 'D:\\data\\kkpoem\\dict\\'
    def dir = new File(dbLocation)

    def cols = ['_id', 'mingcheng', 'zuozhe', 'shipin', 'ticai', 'chaodai', 'guojia', 'fenlei', 'jieduan', 'keben', 'congshu', 'chuchu', 'zhaiyao', 'yuanwen']

    void copyDbs() {

        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                if (!(new File("" + file.getName()).exists()))
                    Files.copy(Paths.get(file.getPath()), Paths.get("" + file.getName()))
            }
        }
    }

    void clearDbs(){
        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                new File("./" + file.getName()).delete()
            }
        }
    }

    def perform() {

        copyDbs()

        def db = new GroovyDataLoader().connectToDb()
        MongoCollection<Document> poemCollection = db.getCollection("poem");


        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")

        def sql_shangxi = Sql.newInstance("jdbc:sqlite:poem_shangxi.db", "", "", "org.sqlite.JDBC")
        def sql_yiwen = Sql.newInstance("jdbc:sqlite:poem_yiwen.db", "", "", "org.sqlite.JDBC")
        def sql_zhujie = Sql.newInstance("jdbc:sqlite:poem_zhujie.db", "", "", "org.sqlite.JDBC")
//        def sql_zuozhe = Sql.newInstance("jdbc:sqlite:poem_zuozhe.db", "", "", "org.sqlite.JDBC")

        def sqlmap = ["shangxi": sql_shangxi, "yiwen": sql_yiwen, "zhujie": sql_zhujie]

        def count = 0
        BasicDBObject query = new BasicDBObject();
        def sql_id_list = "select * from poem where _id>=10000"

        sql_poem.eachRow(sql_id_list) {
            row ->
                def pid = row["_id"]
                query.put("_id", pid);
                def find = poemCollection.find(query)

                if (find.size() == 0) {
                    Document document = new Document();
                    document.put("pid", "" + row["_id"]);
                    cols.each { col ->
                        if (col != '_id')
                            document.put("$col", "" + row[col]);
                    }

                    sqlmap.each { tablename, sql ->

                        sql.eachRow("select " + tablename + " from " + tablename + " where _id=" + pid){ queryRs ->
                            byte[] bytes = queryRs[tablename]
//                            println "byte array length is :"+ bytes.length
//                            println tablename+" is :"+ bytes
                            if (bytes) {
                                int m = (bytes.length / 16);
//                                println "m:"+m
                                int i = m*16;
                                if (i >= 16) {
                                    String result = doSomething(bytes, i);
                                    document.put(tablename, "" + result);
                                }
                            } else {
                                document.put(tablename, "");
                            }
                        }

                    }

                    poemCollection.insertOne(document);
                } else {
                    //poemCollection.up
                }

                println '' + pid
        }

        clearDbs()
    }


    public static byte[] b(byte[] paramArrayOfByte, int paramInt) {
//        println "paramArrayOfByte.length:"+paramArrayOfByte.length
//        println "paramInt:"+paramInt
        try {
            Cipher localCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            localCipher.init(2, new SecretKeySpec("13f439726d2d4522".getBytes(), "AES"));
            byte[] arrayOfByte = localCipher.doFinal(paramArrayOfByte, 0, paramInt);
            return arrayOfByte;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return null;
    }

    private static String doSomething(byte[] arrayOfByte, int i) {
        return new String(b(arrayOfByte, i));
    }


    static def main(args) {
        new PutAllTogether().perform()
    }
}
