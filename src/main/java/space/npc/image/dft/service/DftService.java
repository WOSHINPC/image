package space.npc.image.dft.service;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by luyj on 2017-10-30.
 */
@Service
public class DftService {
    public static final String FILE_PATH_PRE = "/opt/img/";

    public byte[] saveAndTransformImage(CommonsMultipartFile image, String text) throws IOException {
        Date now = new Date();
        String filePath = FILE_PATH_PRE + (now.getYear() + 1900) + "/" + (now.getMonth() + 1) + "/" + now.getDate() + "/" + now.getHours() + "/";
        String originalFileName = image.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String newFileName = UUID.randomUUID() + "." + fileExtension;
        String path = filePath + "/1/";

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(path + newFileName);
        image.transferTo(file);
        Mat mat = dft(path + newFileName, text);
        String dftImagePath = filePath + "/2/";
        file = new File(dftImagePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        Highgui.imwrite(dftImagePath + newFileName, mat);
        MatOfByte mob = new MatOfByte();
        Highgui.imencode("." + fileExtension, mat, mob);
        return mob.toArray();
    }

    public byte[] saveAndIdftImage(CommonsMultipartFile image) throws IOException {
        Date now = new Date();
        String filePath = FILE_PATH_PRE + (now.getYear() + 1900) + "/" + (now.getMonth() + 1) + "/" + now.getDate() + "/" + now.getHours() + "/";
        String originalFileName = image.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String newFileName = UUID.randomUUID() + "." + fileExtension;
        String path = filePath + "/3/";

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(path + newFileName);
        image.transferTo(file);
        Mat mat = idft(path + newFileName);
        String dftImagePath = filePath + "/4/";
        file = new File(dftImagePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        Highgui.imwrite(dftImagePath + newFileName, mat);
        MatOfByte mob = new MatOfByte();
        Highgui.imencode("." + fileExtension, mat, mob);
        return mob.toArray();
    }


    public Mat dft(String filePath, String text) {
        Mat mat = Highgui.imread(filePath);
        Scalar scalar = new Scalar(255, 255, 255);
        Point point = new Point(40, 40);
        mat = transformImageWithText(mat, text, point, 1D, scalar);
        return mat;
    }

    public Mat idft(String filePath) {
        Mat mat = Highgui.imread(filePath);
        mat = transformImage(mat);
        return mat;
    }

    private Mat transformImageWithText(Mat image, String watermarkText, Point point, Double fontSize, Scalar scalar) {
        List<Mat> planes = new ArrayList<>();
        List<Mat> allPlanes = new ArrayList<>();
        Mat complexImage = new Mat();
        // optimize the dimension of the loaded image
        //Mat padded = this.optimizeImageDim(image);
        Mat padded = splitSrc(image, allPlanes);
        padded.convertTo(padded, CvType.CV_32F);
        // prepare the image planes to obtain the complex image
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        // prepare a complex image for performing the dft
        Core.merge(planes, complexImage);
        // dft
        Core.dft(complexImage, complexImage);
        // 频谱图上添加文本

        Core.putText(complexImage, watermarkText, point, Core.FONT_HERSHEY_DUPLEX, fontSize, scalar);
        Core.flip(complexImage, complexImage, -1);
        Core.putText(complexImage, watermarkText, point, Core.FONT_HERSHEY_DUPLEX, fontSize, scalar);
        Core.flip(complexImage, complexImage, -1);
        return antitransformImage(complexImage, allPlanes);
    }

    private Mat splitSrc(Mat mat, List<Mat> allPlanes) {
        if (allPlanes == null) {
            allPlanes = new ArrayList<>();
        }
//        mat = optimizeImageDim(mat);
        Core.split(mat, allPlanes);
        Mat padded;
        if (allPlanes.size() > 0) {
            padded = allPlanes.get(0);
        } else {
            padded = mat;
        }
        return padded;
    }

    public Mat antitransformImage(Mat complexImage, List<Mat> allPlanes) {
        Mat invDFT = new Mat();
        Core.idft(complexImage, invDFT, Core.DFT_SCALE | Core.DFT_REAL_OUTPUT, 0);
        Mat restoredImage = new Mat();
        invDFT.convertTo(restoredImage, CvType.CV_8U);
        if (allPlanes.size() == 0) {
            allPlanes.add(restoredImage);
        } else {
            allPlanes.set(0, restoredImage);
        }
        Mat lastImage = new Mat();
        Core.merge(allPlanes, lastImage);
        return lastImage;
    }

    public Mat transformImage(Mat image) {
        List<Mat> planes = new ArrayList<>();
        Mat complexImage = new Mat();
        // optimize the dimension of the loaded image
        Mat padded = splitSrc(image, null);

        padded.convertTo(padded, CvType.CV_32F);
        // prepare the image planes to obtain the complex image
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        // prepare a complex image for performing the dft
        Core.merge(planes, complexImage);
        // dft
        Core.dft(complexImage, complexImage);
        // optimize the image resulting from the dft operation
        Mat magnitude = this.createOptimizedMagnitude(complexImage);
        planes.clear();
        return magnitude;
    }

    /**
     * Optimize the magnitude of the complex image obtained from the DFT, to
     * improve its visualization
     *
     * @param complexImage the complex image obtained from the DFT
     * @return the optimized image
     */
    private Mat createOptimizedMagnitude(Mat complexImage) {
        // init
        List<Mat> newPlanes = new ArrayList<>();
        Mat mag = new Mat();
        // split the comples image in two planes
        Core.split(complexImage, newPlanes);
        // compute the magnitude
        Core.magnitude(newPlanes.get(0), newPlanes.get(1), mag);

        // move to a logarithmic scale
        Core.add(Mat.ones(mag.size(), CvType.CV_32F), mag, mag);
        Core.log(mag, mag);
        // optionally reorder the 4 quadrants of the magnitude image
        this.shiftDFT(mag);
        // normalize the magnitude image for the visualization since both JavaFX
        // and OpenCV need images with value between 0 and 255
        // convert back to CV_8UC1
        mag.convertTo(mag, CvType.CV_8UC1);
        Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        return mag;
    }

    /**
     * Reorder the 4 quadrants of the image representing the magnitude, after
     * the DFT
     *
     * @param image the {@link Mat} object whose quadrants are to reorder
     */
    private void shiftDFT(Mat image) {
        image = image.submat(new Rect(0, 0, image.cols() & -2, image.rows() & -2));
        int cx = image.cols() / 2;
        int cy = image.rows() / 2;

        Mat q0 = new Mat(image, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(image, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(image, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(image, new Rect(cx, cy, cx, cy));

        Mat tmp = new Mat();
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);

        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);
    }


    @PostConstruct
    private static void loadOpencv() throws Exception {
        System.out.println("######## 开始加载 Opencv ########");
        //获取存放dll文件的绝对路径
        String path = System.getProperty("web.root");
        System.out.println("######## 路径为 : " + path + " ########");
        //将此目录添加到系统环境变量中
        addDirToPath(path);
        //加载相应的dll文件，注意要将'\'替换为'/'
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            System.load(path.replaceAll("\\\\", "/") + "/opencv_java2413.dll");
        } else if (os.toLowerCase().startsWith("linux")) {
            System.load(path.replaceAll("\\\\", "/") + "/libopencv_java2413.so");
        } else {
            System.out.println("os:" + os);
            throw new Exception("系统错误，无法加载opencv");
        }

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("######## Opencv加载完毕 ########");
    }

    private static void addDirToPath(String s) {
        try {
            //获取系统path变量对象
            Field field = ClassLoader.class.getDeclaredField("sys_paths");
            //设置此变量对象可访问
            field.setAccessible(true);
            //获取此变量对象的值
            String[] path = (String[]) field.get(null);
            //创建字符串数组，在原来的数组长度上增加一个，用于存放增加的目录
            String[] tem = new String[path.length + 1];
            //将原来的path变量复制到tem中
            System.arraycopy(path, 0, tem, 0, path.length);
            //将增加的目录存入新的变量数组中
            tem[path.length] = s;
            //将增加目录后的数组赋给path变量对象
            field.set(null, tem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
