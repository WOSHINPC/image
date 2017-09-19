package space.npc.image.dft.controller;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import space.npc.image.dft.util.DFTUtil;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * Created by luyj on 2017-09-18.
 */
@Controller
@RequestMapping("/")
public class TestController {

    @RequestMapping("/test")
    @ResponseBody
    public Object testdft() {
        DFTUtil instance = DFTUtil.getInstance();
        Mat mat = Highgui.imread("d:/temp/1.jpg");
        Scalar scalar = new Scalar(255,255,255);
        Point point = new Point(20,30);
        instance.transformImageWithText(mat,"TEXT",point,0.9,scalar);
        mat = instance.antitransformImage();
        Highgui.imwrite("d:/temp/2.jpg",mat);
        return "{\"code\":0}";
    }
    @RequestMapping("/test2")
    @ResponseBody
    public Object test2dft() {
        DFTUtil instance = DFTUtil.getInstance();
//        Mat mat = instance.antitransformImage();
        Mat mat = Highgui.imread("d:/temp/2.jpg");
        mat = instance.transformImage(mat);
        Highgui.imwrite("d:/temp/3.jpg",mat);
        return "{\"code\":0}";
    }

    @RequestMapping("/test3")
    @ResponseBody
    public Object test3() {
//        DFTUtil instance = DFTUtil.getInstance();
        Mat mat = Highgui.imread("d:/temp/1.jpg");

        Mat temp = new Mat(50, 100, mat.type(),new Scalar(255,255,255));

        Core.putText(temp, "TEXT", new Point(10, 20), Core.FONT_HERSHEY_DUPLEX, 0.5,  new Scalar(0,0,0));
        Rect rect = new Rect(0, 0, temp.cols(), temp.rows());
        Mat roi = new Mat(mat,rect);
        Highgui.imwrite("d:/temp/4.jpg",roi);

        temp.copyTo(roi);
        Highgui.imwrite("d:/temp/5.jpg",mat);
        Highgui.imwrite("d:/temp/6.jpg",temp);
        Highgui.imwrite("d:/temp/7.jpg",roi);
        return "{\"code\":0}";
    }


    @PostConstruct
    private static void loadOpencv(){
        System.out.println("######## 开始加载 Opencv ########");
        //获取存放dll文件的绝对路径
        String path = System.getProperty("web.root");
        System.out.println("######## 路径为 : " + path + " ########");
        //将此目录添加到系统环境变量中
        addDirToPath(path);
        //加载相应的dll文件，注意要将'\'替换为'/'
        System.load(path.replaceAll("\\\\","/")+"/opencv_java2413.dll");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("######## Opencv加载完毕 ########");
    }

    private static void addDirToPath(String s){
        try {
            //获取系统path变量对象
            Field field=ClassLoader.class.getDeclaredField("sys_paths");
            //设置此变量对象可访问
            field.setAccessible(true);
            //获取此变量对象的值
            String[] path=(String[])field.get(null);
            //创建字符串数组，在原来的数组长度上增加一个，用于存放增加的目录
            String[] tem=new String[path.length+1];
            //将原来的path变量复制到tem中
            System.arraycopy(path,0,tem,0,path.length);
            //将增加的目录存入新的变量数组中
            tem[path.length]=s;
            //将增加目录后的数组赋给path变量对象
            field.set(null,tem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
