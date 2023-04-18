package ua.knu.ynortman;

import lombok.Data;

@Data
public class GptMessage {
    private String role;
    private String content;
}
