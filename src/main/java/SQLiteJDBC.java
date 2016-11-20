import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.PrintWriter;
import java.sql.*;

public class SQLiteJDBC {

    public static byte[] b(byte[] paramArrayOfByte, int paramInt) {
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


    public static void main(String args[]) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:poem_shangxi.db");
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
//            String insertSql = "INSERT INTO shangxi (_ID,SHANGXI) " +
//                    "VALUES (2, '我是一个测试字符串');";
//            stmt.executeUpdate(insertSql);


            String sql = "select _id,shangxi from shangxi where _id =1 ";
            ResultSet rs = stmt.executeQuery(sql);
            String shangxiStr = "";
            while (rs.next()) {
                int id = rs.getInt("_id");
                byte[] bytes = rs.getBytes(2);
                System.out.println("id:" + id +", lenth:"+bytes.length);
                System.out.println("shangxi is :"+ bytes);
                if (bytes != null) {
                    int i = 16 * (bytes.length / 16);
                    if (i >= 16) {
                        String result = doSomething(bytes, i);
                        System.out.print(result + " ");
                    }
                }

            }

            PrintWriter pw = new PrintWriter("shangxi.txt");
            StringBuilder sb = new StringBuilder();
            for(int j=0;j<256;j++){
                for(int i=0;i<shangxiStr.length();i++){
                    char c1 = shangxiStr.charAt(i);
                    String s = String.valueOf((char)(c1*j));
                    sb.append(s);
                }
//                String s = sb.substring(0, 20).toString();
//                if(s.startsWith("g�p")){
//                    continue;
//                }

                //System.out.println("shangxi:" + sb.toString());
                if(sb.length()>20){
                    pw.println(j+"==>"+sb.substring(0, 20).toString());
                } else {
                    pw.println(j+"==>"+sb.toString());
                }

//                Thread.sleep(1000);
            }
            pw.close();

//            System.out.println("shangxi:" + (shangxiStr.length()/16*16));

//            for(int j=1;j<256;j++)
//            {
//                StringBuilder sss = new StringBuilder();
//                String test= "我是古诗赏析";
//                for(int i=0;i<test.length();i++){
//                    char c1 = test.charAt(i);
//                    String s = String.valueOf(((char)(c1+j)));
//                    sss.append(s);
//                }
//                System.out.println("test:" + sss.toString());
//            }


            stmt.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Table created successfully");
    }
}