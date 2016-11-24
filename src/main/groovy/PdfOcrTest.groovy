import net.sourceforge.tess4j.Tesseract

/**
 * Created by SHAOPENGXIANG on 2016/11/24.
 */
class PdfOcrTest {

    def tess4jPath = "C:\\Dev\\tess4j\\tess4j-master\\src\\main\\resources"
    static void main(args){
        new PdfOcrTest().perform()
    }

    void perform() {
        println "os.name:"+ System.getProperty("os.name")
        println "os.arch:"+ System.getProperty("os.arch")
//        println "jna.library.path:"+ System.getProperty("jna.library.path")
        System.out.println(""+new File(".").getAbsolutePath());
        System.setProperty("jna.library.path", "32".equals(System.getProperty("sun.arch.data.model")) ? tess4jPath+"/win32-x86" : tess4jPath+"/win32-x86-64");
        System.setProperty("jna.debug_load", "true")
        System.setProperty("jna.debug_load.jna", "true")
        try {
            File pngFile = new File("C:\\Dev\\gushiwen\\libaishixuan\\workingimage028.png");
            def tesseract = new Tesseract();
            tesseract.setLanguage("chi_sim")
            String s = tesseract.doOCR(pngFile);
            System.out.println(s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
