package ua.knu.ynortman;

import lombok.Data;

@Data
public class GptChoices {
    private GptMessage message;
    private String finish_reason;
    private int index;
}
