package na.bot.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import na.bot.service.UpdateProducer;
import na.bot.service.implementations.UpdateProducerImpl;
import na.bot.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static na.bot.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils;
    private UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update){
        if(update==null){
            log.error("Received msg is null");
            return;
        }

        if(update.getMessage()!= null){
            distributeMsgByType(update);
        } else log.error("Unsupported msg type is received: " + update);
    }

    public void distributeMsgByType(Update update){
        var message = update.getMessage();
        if(message.hasText()){
            processTextMsg(update);
        } else if(message.hasDocument()){
            processDocMsg(update);
        } else if(message.hasPhoto()){
            processPhotoMsg(update);
        } else setUnsupportedMsgView(update);
    }

    private void setUnsupportedMsgView(Update update) {
        var sendMessage = messageUtils.generateSendMsgWithText(update, "Your msg type is not supported");
        setMsgView(sendMessage);
    }

    private void setFileReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMsgWithText(update, "I have received your file, wait a bit pls");
        setMsgView(sendMessage);
    }

    public void setMsgView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processPhotoMsg(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileReceivedView(update);
    }

    public void processTextMsg(Update update){
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }
    public void processDocMsg(Update update){
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileReceivedView(update);
    }
}
