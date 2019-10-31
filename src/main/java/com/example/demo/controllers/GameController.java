package com.example.demo.controllers;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class GameController {
    Log log = LogFactory.getLog(GameController.class);

    @MessageMapping("/create")
    @SendTo("/topic/board")
    public String createGame() throws IllegalArgumentException {
        log.info("createGame");
        return "createGame";
    }

    @MessageMapping("/move/{uuid}")
    @SendTo("/topic/move/{uuid}")
    public String makeMove() throws IllegalArgumentException {
        log.info("makeMoveZ");
        return "makeMove";
    }
}
