package fun.javierchen.ainocodeapplication;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan("fun.javierchen.ainocodeapplication.mapper")
public class AiNoCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiNoCodeApplication.class, args);
    }

}
