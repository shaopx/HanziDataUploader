/**
 * Created by SHAOPENGXIANG on 2016/11/21.
 */
public class CodeTest {


    public static void showUnicode(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.printf("\\u%x", (int)str.charAt(i));
        }
        System.out.println();
    }

    public static void main(String[] args)throws Exception{
        String str = "\u865e";
        System.out.println(""+str);

        String str2 = "锟斤拷";
        showUnicode(str);



        //"你好"的GBK编码数据
        byte[] gbkData = {(byte)0xd3, (byte)0xdd, (byte)0xca, (byte)0xc0};
        //"你好"的BIG5编码数据
        byte[] big5Data = {(byte)0xa7, (byte)0x41, (byte)0xa6, (byte)0x6e};

        //构造String，解码为Unicode

        String strFromGBK = new String(gbkData, "GBK");
//        String strFromBig5 = new String(big5Data, "BIG5");

        //分别输出Unicode序列

        showUnicode(strFromGBK);
        System.out.println("strFromGBK:"+strFromGBK);
//        showUnicode(strFromBig5);
    }
}
