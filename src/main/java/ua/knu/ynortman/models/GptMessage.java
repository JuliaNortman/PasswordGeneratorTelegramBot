package ua.knu.ynortman.models;

import lombok.Data;

@Data
public class GptMessage {
    private String role;
    private String content;
}
