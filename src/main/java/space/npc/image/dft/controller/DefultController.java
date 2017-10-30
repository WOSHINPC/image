package space.npc.image.dft.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import space.npc.image.dft.service.DftService;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by luyj on 2017-10-30.
 */
@Controller
@RequestMapping("/")
public class DefultController {
    @Autowired
    private DftService dftService;

    @RequestMapping("/dft")
    public void dft(HttpServletResponse response,@RequestParam("image") CommonsMultipartFile image) throws IOException {
        byte[] bytes = dftService.saveAndTransformImage(image, "TEST");
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.close();
    }

    @RequestMapping("/idft")
    public void idft(HttpServletResponse response,@RequestParam("image") CommonsMultipartFile image) throws IOException {
        byte[] bytes = dftService.saveAndIdftImage(image);
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.close();
    }

}
