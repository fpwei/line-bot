package org.fpwei.line.server.handler;

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import org.fpwei.line.core.dao.ImageDao;
import org.fpwei.line.core.dao.PostDao;
import org.fpwei.line.core.entity.Image;
import org.fpwei.line.core.entity.Post;
import org.fpwei.line.server.annotation.Command;
import org.fpwei.line.server.enums.BeautyParameter;
import org.fpwei.line.server.enums.ImageParameter;
import org.fpwei.line.server.enums.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command({"beauty", "表特"})
public class BeautyHandler extends AbstractCommandHandler {

    private final static int DEFAULT_QUERY_SIZE = 10;

    @Autowired
    private PostDao postDao;

    @Autowired
    private ImageDao imageDao;

    @Override
    protected Message execute(Map parameterMap) {
        Integer size = parameterMap.get(BeautyParameter.NUMBER) == null ? DEFAULT_QUERY_SIZE : Integer.valueOf(parameterMap.get(BeautyParameter.NUMBER).toString());

        List<Post> posts;
        String altText;
        if (parameterMap.containsKey(BeautyParameter.RECENT)) {
            posts = postDao.findAll(new PageRequest(0, size, new Sort(Sort.Direction.DESC, "date"))).getContent();
            altText = "最新表特正妹";
        } else {
            posts = postDao.findPostsByRandom(size);
            altText = "隨選表特正妹";
        }

        List<Image> images = posts.stream()
                .map(post -> imageDao.findFirstByPostId(post.getId()))
                .collect(Collectors.toList());


        List<ImageCarouselColumn> columns = images.stream()
                .map(image -> {
                    String url = generateImageUrl(image.getPath());
                    String data = String.format("image %s %d", ImageParameter.POST.getValue(), image.getPost().getId());
                    Action action = new PostbackAction("View More", data);
                    return new ImageCarouselColumn(url, action);
                })
                .collect(Collectors.toList());


        ImageCarouselTemplate template = new ImageCarouselTemplate(columns);

        return new TemplateMessage(altText, template);
    }

    private String generateImageUrl(String imagePath) {
        String path = imagePath.replace("/data/img/", "").replace(".jpg", "");

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .pathSegment("image", path)
                .build()
                .toUriString();
    }

    @Override
    protected Parameter getParameter(String value) {
        return BeautyParameter.getParameter(value);
    }
}
