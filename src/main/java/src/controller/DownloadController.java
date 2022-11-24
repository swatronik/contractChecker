package src.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import src.util.FileUtil;
import src.util.MediaTypeUtils;

import javax.servlet.ServletContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
public class DownloadController {

    @Autowired
    private ServletContext servletContext;

    private static final String DEFAULT_FILE_NAME = "data/query/%s/%s";
    private static final String DEFAULT_7Z_DIR = "data/archive/";

    @RequestMapping("/download7z/{folder}/{name}")
    public ResponseEntity<InputStreamResource> download7zFile(
            @PathVariable("folder") String folder,
            @PathVariable("name") String name) throws IOException {

        String fileName = String.format(DEFAULT_FILE_NAME, folder, name);
        String filename7z = String.format("%s-%s.zip", folder, name);

        FileUtil.createZipFromPath(fileName, filename7z);

        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, DEFAULT_7Z_DIR + filename7z);
        System.out.println("fileName: " + filename7z);
        System.out.println("mediaType: " + mediaType);

        File file = new File(DEFAULT_7Z_DIR + filename7z);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType)
                .contentLength(file.length())
                .body(resource);
    }

    @RequestMapping("/downloadBody/{folder}/{name}")
    public ResponseEntity<InputStreamResource> downloadBody(
            @PathVariable("folder") String folder,
            @PathVariable("name") String name) throws IOException {

        String fileName = String.format(DEFAULT_FILE_NAME, folder, name);
        String filename7z = String.format("%s-%s.zip", folder, name);

        FileUtil.createZipFromPath(fileName, filename7z);

        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, DEFAULT_7Z_DIR + filename7z);
        System.out.println("fileName: " + filename7z);
        System.out.println("mediaType: " + mediaType);

        File file = new File(DEFAULT_7Z_DIR + filename7z);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType)
                .contentLength(file.length())
                .body(resource);
    }
}
