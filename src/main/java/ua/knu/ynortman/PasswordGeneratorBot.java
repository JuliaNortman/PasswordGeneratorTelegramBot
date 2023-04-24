package ua.knu.ynortman;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.knu.ynortman.models.GptResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ua.knu.ynortman.constants.Constants.*;

@Slf4j
public class PasswordGeneratorBot extends TelegramLongPollingBot {

    private enum UserState {
        DEFAULT,
        AWAITING_NUMBER
    }

    private final Map<Long, UserState> userStates = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            UserState currentState = userStates.getOrDefault(message.getChatId(), UserState.DEFAULT);

            if (message.getText().equalsIgnoreCase(START_BUTTON_ID)) {
                sendStartMessage(message.getChatId().toString());
            } else if(currentState.equals(UserState.AWAITING_NUMBER)) {
                if (!NUMBER_PATTERN.matcher(message.getText()).matches()) {
                    sendTextMessage(message.getChatId(), "Invalid input! Please enter only numbers");
                    userStates.put(message.getChatId(), UserState.DEFAULT);
                    return;
                }
                userStates.put(message.getChatId(), UserState.DEFAULT);
                String password = generatePassphrase(true, Integer.parseInt(message.getText()));
                sendTextMessage(message.getChatId(), "Generated Passphrase: " + password);
            }
        } else if(update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(GENERATE_PASSWORD_COMMAND)) {
                yesNoMenu(chatId, PASSWORD_INCLUDE_SPECIAL_CHARS_YES_ID, PASSWORD_INCLUDE_SPECIAL_CHARS_NO_ID, INCLUDE_SPECIAL_CHARS_PROMPT);
            } else if (callbackData.equals(GENERATE_PASSPHRASE_COMMAND)) {
                yesNoMenu(chatId, PASSWORD_LENGTH_LIMIT_YES, PASSWORD_LENGTH_LIMIT_NO, LENGTH_LIMIT_PROMPT);
            } else if (callbackData.equals(PASSWORD_INCLUDE_SPECIAL_CHARS_YES_ID)) {
                String password = generatePassword(true);
                sendTextMessage(chatId, "Generated Password: " + password);
            } else if (callbackData.equals(PASSWORD_INCLUDE_SPECIAL_CHARS_NO_ID)) {
                String password = generatePassword(false);
                sendTextMessage(chatId, "Generated Password: " + password);
            } else if (callbackData.equals(PASSWORD_LENGTH_LIMIT_NO)) {
                String password = generatePassphrase(false, 0);
                sendTextMessage(chatId, "Generated Passphrase: " + password);
            } else if (callbackData.equals(PASSWORD_LENGTH_LIMIT_YES)) {
                userStates.put(chatId, UserState.AWAITING_NUMBER);
                sendTextMessage(chatId, ENTER_MAX_LENGTH_PROMPT);
            }
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void yesNoMenu(long chatId, String yesId, String noId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("Yes")
                .callbackData(yesId)
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("No")
                .callbackData(noId)
                .build());

        keyboard.add(row1);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendStartMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Choose an option:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("Generate Password")
                .callbackData(GENERATE_PASSWORD_COMMAND)
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("Generate Passphrase")
                .callbackData(GENERATE_PASSPHRASE_COMMAND)
                .build());

        keyboard.add(row1);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return System.getenv(TG_BOT_TOKEN_ENV_NAME);
    }

    public String generatePassword(boolean includeSpecialChars) {
        String prompt;
        if(includeSpecialChars) {
            prompt = "You are a password generator that generates reliable and secure passwords. Generate a reliable " +
                    "password based on the following description: Length - between 12 and 14 characters; include at " +
                    "least one uppercase, at least one lowercase letter, at least one digit and at least one special " +
                    "symbol from range !@#$%^&* The most important requirment is that password should not resemble any " +
                    "common words! Return just password in answer, no other words.";
        } else {
            prompt = "You are a password generator that generates reliable and secure passwords. Generate a reliable " +
                    "password based on the following description: Length - between 12 and 14 characters; include at " +
                    "least one uppercase, at least one lowercase letter, at least one digit. The most important " +
                    "requirment is that password should not resemble any common words! Return just password in answer, " +
                    "no other words.";
        }
        return chatWithGpt(prompt);
    }

    public String generatePassphrase(boolean lengthLimit, int limit) {
        String prompt;
        if(lengthLimit) {
            prompt = "You are a password generator tool. Generate a reliable password with at max {limit} characters, " +
                    "at least one uppercase and at least one lowercase letter, at least one number, at least one " +
                    "special character from the six randomly chosen nouns. All six nouns have to be used in password. " +
                    "Password has to follow the length limitation, if it exceeds it then replace couple of letters " +
                    "with numbers or symbols but do not throw out words. Your answer must contain just resulted " +
                    "password, no other words or symbols";
            prompt = prompt.replace("{limit}", String.valueOf(limit));
        } else {
            prompt = "You are a passphrase generator that generates reliable and secure passphrases. Generate a " +
                    "reliable passphrase based on the following description: Use six random words that are not related " +
                    "to each other; choose only one special symbol from range _@#$* and link words in passphrase using " +
                    "it. Choose randomly whether to capitalize each word or not. Return just passphrase in answer, no " +
                    "other words.";
        }
        return chatWithGpt(prompt);
    }

    public String chatWithGpt(String prompt) {

        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        // Prepare request data
        String requestBodyJson = "{\n" +
                "    \"model\": \"gpt-3.5-turbo\",\n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"system\",\n" +
                "            \"content\": \"{prompt}\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"max_tokens\": 60,\n" +
                "    \"temperature\": 0.5\n" +
                "}";
        requestBodyJson = requestBodyJson.replace("{prompt}", prompt);
        log.debug("Request body: {}", requestBodyJson);
        RequestBody requestBody = RequestBody.create(requestBodyJson, MediaType.parse("application/json; charset=utf-8"));

        // Prepare the API request
        Request request = new Request.Builder()
                .url(OPEN_AI_API_CHAT_URL)
                .header("Authorization", "Bearer " + System.getenv(OPEN_AI_API_KEY_ENV_NAME))
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try {
            // Execute the API request
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Parse the API response
                String responseBody = response.body().string();
                GptResponse gptResponse = gson.fromJson(responseBody, GptResponse.class);

                if (gptResponse.getChoices().length > 0) {
                    return gptResponse.getChoices()[0].getMessage().getContent().trim();
                }
            } else {
                log.error("Response: {}", response);
                log.error("Error: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Error generating password";
    }
}
