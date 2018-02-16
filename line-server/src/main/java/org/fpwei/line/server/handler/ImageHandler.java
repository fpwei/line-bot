package org.fpwei.line.server.handler;

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import lombok.extern.slf4j.Slf4j;
import org.fpwei.line.core.common.CommonRuntimeException;
import org.fpwei.line.core.dao.ImageDao;
import org.fpwei.line.core.entity.Image;
import org.fpwei.line.server.annotation.Command;
import org.fpwei.line.server.enums.ImageParameter;
import org.fpwei.line.server.enums.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Command("image")
public class ImageHandler extends AbstractCommandHandler {
    private static final int DEFAULT_PAGE_SIZE = 10;
    @Autowired
    private ImageDao imageDao;

    @Override
    protected Parameter getParameter(String value) {
        return ImageParameter.getParameter(value);
    }

    @Override
    protected Message execute(Map<Parameter, Object> parameterMap) {

        if (parameterMap.containsKey(ImageParameter.POST) && parameterMap.get(ImageParameter.POST) != null) {
            int postId = Integer.valueOf(parameterMap.get(ImageParameter.POST).toString());
            int page = parameterMap.containsKey(ImageParameter.PAGE) ? Integer.valueOf(parameterMap.get(ImageParameter.PAGE).toString()) : 1;

            List<Image> images = imageDao.findImagesByPostId(postId, new PageRequest((page - 1), DEFAULT_PAGE_SIZE)); //page base index = 0

            List<ImageCarouselColumn> columns = images.stream()
                    .map(image -> {
                        String url = generateImageUrl(image.getPath());
                        Action action = new PostbackAction(null, "null");
                        return new ImageCarouselColumn(url, action);
                    })
                    .collect(Collectors.toList());

            //if exist more images, set get next page action in last column
            int totalPage = (imageDao.countByPostId(postId) + DEFAULT_PAGE_SIZE - 1) / DEFAULT_PAGE_SIZE;
            if (totalPage > page) {
                Image image = images.get(DEFAULT_PAGE_SIZE - 1);

                String url = generateImageUrl(image.getPath());
                String data = String.format("image %s %d %s %d", ImageParameter.POST.getValue(), postId, ImageParameter.PAGE.getValue(), page + 1);
                Action action = new PostbackAction("Next Page", data);
                ImageCarouselColumn lastColumn = new ImageCarouselColumn(url, action);

                columns.set(DEFAULT_PAGE_SIZE - 1, lastColumn);
            }

            ImageCarouselTemplate template = new ImageCarouselTemplate(columns);

            String postTitle = images.get(0).getPost().getTitle();

            return new TemplateMessage(String.format("%s(%d/%d)", postTitle, page, totalPage), template);
        } else {
            log.warn("post id must be provided");
            throw new CommonRuntimeException("post id must be provided");
        }

    }

    private String generateImageUrl(String imagePath) {
        String path = imagePath.replace("/data/img/", "").replace(".jpg", "");

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .pathSegment("image", path)
                .build()
                .toUriString();
    }
}
