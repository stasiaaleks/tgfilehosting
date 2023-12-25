package na.bot.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static na.bot.model.RabbitQueue.*;

@Configuration
public class RabbitMQConfiguration {
    @Bean
    public MessageConverter jsonMsgConverter(){
        return new Jackson2JsonMessageConverter();
    }
}