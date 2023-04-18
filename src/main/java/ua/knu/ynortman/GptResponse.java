package ua.knu.ynortman;

import lombok.Data;

@Data
public class GptResponse {
    private String id;
    private String object;
    private GptChoices[] choices;
}

