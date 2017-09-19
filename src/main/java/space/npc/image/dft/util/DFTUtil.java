package space.npc.image.dft.util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DFTUtil {
    private static List<Mat> planes;
    private static List<Mat> allPlanes;
    private static Mat complexImage;

    private DFTUtil() {
    }

    private static final DFTUtil dftUtil = new DFTUtil();

    public static DFTUtil getInstance() {
        planes = new ArrayList<>();
        allPlanes = new ArrayList<>();
        complexImage = new Mat();
        return dftUtil;
    }

    private Mat splitSrc(Mat mat) {
        if (!allPlanes.isEmpty()) {
            allPlanes.clear();
        }
//        mat = optimizeImageDim(mat);
        Core.split(mat, allPlanes);
        Mat padded = new Mat();
        if (allPlanes.size() > 1) {
            for (int i = 0; i < allPlanes.size(); i++) {
                if (i == 0) {
                    padded = allPlanes.get(i);
                    break;
                }
            }
        } else {
//            padded = mat;
        }
        return padded;
    }

    public Mat transformImage(Mat image) {
        // planes数组中存的通道数若开始不为空,需清空.
        if (!planes.isEmpty()) {
            planes.clear();
        }
        // optimize the dimension of the loaded image
        Mat padded = splitSrc(image);

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

    public void transformImageWithText(Mat image, String watermarkText, Point point, Double fontSize, Scalar scalar) {
        // planes数组中存的通道数若开始不为空,需清空.
        if (!planes.isEmpty()) {
            planes.clear();
        }
        // optimize the dimension of the loaded image
        //Mat padded = this.optimizeImageDim(image);
        Mat padded = splitSrc(image);
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
//
//        Mat temp = new Mat(50, 100, complexImage.type());
//        Core.putText(temp, "TEXT", new Point(10, 10), Core.FONT_HERSHEY_DUPLEX, 0.5, new Scalar(0, 0, 0));
//        Rect rect = new Rect(10, 10, temp.cols(), temp.rows());
//        Mat roi = new Mat(complexImage, rect);
//        temp.copyTo(roi);
//        Core.flip(complexImage, complexImage, -1);
//        roi = new Mat(complexImage, rect);
//        temp.copyTo(roi);
//        Core.flip(complexImage, complexImage, -1);

        planes.clear();

    }

    public Mat antitransformImage() {
        Mat invDFT = new Mat();
        Core.idft(complexImage, invDFT, Core.DFT_SCALE | Core.DFT_REAL_OUTPUT, 0);
        Mat restoredImage = new Mat();
        invDFT.convertTo(restoredImage, CvType.CV_8U);
        allPlanes.set(0, restoredImage);
        Mat lastImage = new Mat();
        Core.merge(allPlanes, lastImage);
        return lastImage;
    }

    /**
     * 为加快傅里叶变换的速度，对要处理的图片尺寸进行优化
     *
     * @param image the {@link Mat} to optimize
     * @return the image whose dimensions have been optimized
     */
    private Mat optimizeImageDim(Mat image) {
        // init
        Mat padded = new Mat();
        // get the optimal rows size for dft
        int addPixelRows = Core.getOptimalDFTSize(image.rows());
        // get the optimal cols size for dft
        int addPixelCols = Core.getOptimalDFTSize(image.cols());
        // apply the optimal cols and rows size to the image
        Imgproc.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
                Imgproc.BORDER_CONSTANT, Scalar.all(0));

        return padded;
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
}