package ua.knu.ynortman.constants;

import java.util.regex.Pattern;

public class Constants {
    public static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");
    public static final String INCLUDE_SPECIAL_CHARS_PROMPT = "Should special characters - !@#$%^&*: be included?";
    public static final String LENGTH_LIMIT_PROMPT = "Should passphrase be limited in length?";
    public static final String ENTER_MAX_LENGTH_PROMPT = "Enter max length:";
    public static final String START_BUTTON_ID = "/start";
    public static final String PASSWORD_INCLUDE_SPECIAL_CHARS_YES_ID = "password_include_special_chars_yes";
    public static final String PASSWORD_INCLUDE_SPECIAL_CHARS_NO_ID = "password_include_special_chars_no";
    public static final String PASSWORD_LENGTH_LIMIT_YES = "passphrase_length_limit_yes";
    public static final String PASSWORD_LENGTH_LIMIT_NO = "passphrase_length_limit_no";
    public static final String GENERATE_PASSWORD_COMMAND = "generate_password";
    public static final String GENERATE_PASSPHRASE_COMMAND = "generate_passphrase";


    public static final String BOT_USERNAME =  "pass_gen_with_chatgpt_bot";
    public static final String OPEN_AI_API_CHAT_URL = "https://api.openai.com/v1/chat/completions";

    public static final String TG_BOT_TOKEN_ENV_NAME = "TELERAM_BOT_TOKEN";
    public static final String OPEN_AI_API_KEY_ENV_NAME = "OPEN_AI_API_KEY";
}
