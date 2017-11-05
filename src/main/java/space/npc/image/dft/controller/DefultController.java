package space.npc.image.dft.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    @ResponseBody
    public void dft(HttpServletResponse response,@RequestParam("image") CommonsMultipartFile image,String msg) throws IOException {
        System.out.println(msg);
        byte[] bytes = dftService.saveAndTransformImage(image, msg);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + image.getOriginalFilename() + "\";");
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.close();
    }

    @RequestMapping("/idft")
    @ResponseBody
    public void idft(HttpServletResponse response,@RequestParam("image") CommonsMultipartFile image) throws IOException {
        byte[] bytes = dftService.saveAndIdftImage(image);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"npc_" + image.getOriginalFilename() + "\";");
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.close();
    }

}
