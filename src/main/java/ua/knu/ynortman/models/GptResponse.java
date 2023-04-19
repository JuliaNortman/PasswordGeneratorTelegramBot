package ua.knu.ynortman.models;

import lombok.Data;

@Data
public class GptResponse {
    private String id;
    private String object;
    private GptChoices[] choices;
}

