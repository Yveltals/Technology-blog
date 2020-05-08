package com.zzx.mq;


import com.zzx.config.RabbitMQConfig;
import com.zzx.dao.BlogDao;
import com.zzx.model.pojo.Blog;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 更新博客的队列消费者
 *
 * @blame mqpearh
 */
@Component
@RabbitListener(queues = RabbitMQConfig.BLOG_QUEUE)
public class BlogListener {

    @Autowired
    private BlogDao blogDao;

    @RabbitHandler
    public void updateBlog(Blog blog) {
        blogDao.updateBlog(blog);
    }
}
