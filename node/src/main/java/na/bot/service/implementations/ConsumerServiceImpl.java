package na.bot.service.implementations;

import lombok.extern.log4j.Log4j;
import na.bot.service.ConsumerService;
import na.bot.service.MainService;
import na.bot.service.ProducerService;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import static na.bot.model.RabbitQueue.TEXT_MESSAGE_UPDATE;
import static na.bot.model.RabbitQueue.PHOTO_MESSAGE_UPDATE;
import static na.bot.model.RabbitQueue.DOC_MESSAGE_UPDATE;

@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    /*private final ProducerService producerService; //why here we can use instance of an interface
    public ConsumerServiceImpl(ProducerService producerService) {
        this.producerService = producerService;
        log.debug("Test");
    }*/

    private final MainService mainService;
    public ConsumerServiceImpl(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE: Text msg received");
        mainService.processTextMessage(update);

     /*   var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.producerAnswer(sendMessage);*/
    }

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdates(Update update) {
        log.debug("NODE: Photo msg received");

    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdates(Update update) {
        log.debug("NODE: Doc msg received");

    }
}
