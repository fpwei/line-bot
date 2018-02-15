package org.fpwei.line.server.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class ImageController {
    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${org.fpwei.line.image.path}")
    private String imageBasePath;

    @GetMapping(path = "image/{post}/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Resource getImage(@PathVariable String post, @PathVariable String name) {
        File file = FileUtils.getFile(imageBasePath, post, name + ".jpg");

        if (file.exists()) {
            return resourceLoader.getResource(String.format("file:%s", file.getAbsolutePath()));
        }else{
            return null;
        }
    }
}
