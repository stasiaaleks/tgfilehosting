package na.bot.service.implementations;

import lombok.extern.log4j.Log4j;
import na.bot.dao.AppUserDAO;
import na.bot.dao.RawDataDAO;
import na.bot.entity.AppUser;
import na.bot.entity.RawData;
import na.bot.service.MainService;
import na.bot.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static na.bot.entity.enums.UserState.BASIC_STATE;
import static na.bot.entity.enums.UserState.WAITING_FOR_EMAIL_STATE;
import static na.bot.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService; // how to create an instance of an interface
    private final AppUserDAO appUserDAO;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        if(CANCEL.equals(text)){
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)){
            output = processServiceCommand(appUser, text);
        } else if (WAITING_FOR_EMAIL_STATE.equals(userState)){
            // TODO: add email processing
        } else {
            log.error("Unknown user state: " + userState);
            output = "Unknown stuff. Print /cancel and try again";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);



    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if(contentSendindNotAllowed(chatId, appUser)){
            return;
        }

        // TODO: add doc downloading
        var answer = "Doc was downloaded, nice :) Here`s your link to download it: big69.com";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if(contentSendindNotAllowed(chatId, appUser)){
            return;
        }

        // TODO: add photo downloading
        var answer = "Photo was downloaded, nice :) Here`s your link to download it: big69.com";
        sendAnswer(answer, chatId);
    }

    private boolean contentSendindNotAllowed(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if(!appUser.isActive()){
            var error = "Register or log in to send content";
            sendAnswer(error,chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)){
            var error = "Cancel current command with pressing /cancel to be able to send files.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if(REGISTRATION.equals(cmd)){
            //TODO: add registration
            return "Sorry, I am still working on it:)";
        } else if(HELP.equals(cmd)){
            return help();
        } else if(START.equals(cmd)){
            return "Hi cutie. If you want to check all the commands, press /help";
        } else {
            return "Idk what you are saying bro. If you want to check all the commands, press /help";
        }
    }

    private String help() {
        return "Press any of my buttons that you want :)\n" +
                "/cancel to cancel command\n" +
                "/registration to register yourself in this bot\n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Nice, command was cancelled";
    }

    private AppUser findOrSaveAppUser(Update update){
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO change to default state after registration
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();

            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder().event(update).build(); //how does builder work
        rawDataDAO.save(rawData);
    }
}
