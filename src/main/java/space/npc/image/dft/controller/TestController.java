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

    private String filePath = "d:/temp/1.png";
    private String getFilePath(String num){
        return filePath.replace("1",num);
    }
    @RequestMapping("/test")
    @ResponseBody
    public Object testdft() {
        DFTUtil instance = DFTUtil.getInstance();
        Mat mat = Highgui.imread(filePath);
        Scalar scalar = new Scalar(255,255,255);
        Point point = new Point(20,30);
        instance.transformImageWithText(mat,"TEXT",point,0.9,scalar);

        mat = instance.antitransformImage();
        Highgui.imwrite(getFilePath("2"),mat);
        return "{\"code\":0}";
    }
    @RequestMapping("/test2")
    @ResponseBody
    public Object test2dft() {
        DFTUtil instance = DFTUtil.getInstance();
//        Mat mat = instance.antitransformImage();
        Mat mat = Highgui.imread(getFilePath("2"));
        mat = instance.transformImage(mat);
        Highgui.imwrite(getFilePath("3"),mat);
        return "{\"code\":0}";
    }

    @RequestMapping("/test3")
    @ResponseBody
    public Object test3() {
//        DFTUtil instance = DFTUtil.getInstance();
        Mat mat = Highgui.imread(filePath);

        Mat temp = new Mat(50, 100, mat.type(),new Scalar(255,255,255));

        Core.putText(temp, "TEXT", new Point(10, 20), Core.FONT_HERSHEY_DUPLEX, 0.5,  new Scalar(0,0,0));
        Rect rect = new Rect(0, 0, temp.cols(), temp.rows());
        Mat roi = new Mat(mat,rect);
        Highgui.imwrite(getFilePath("4"),roi);

        temp.copyTo(roi);
        Highgui.imwrite(getFilePath("5"),mat);
        Highgui.imwrite(getFilePath("6"),temp);
        Highgui.imwrite(getFilePath("7"),roi);
        return "{\"code\":0}";
    }

}
