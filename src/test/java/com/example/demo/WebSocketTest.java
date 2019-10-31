package com.example.demo;

import com.example.demo.controllers.GameControllerTest;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rhzeffa on 16/05/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@CommonsLog(topic = "Debian")
//@SpringApplicationConfiguration(classes = App.class)
//@WebIntegrationTest
public class WebSocketTest {
    Log LOGGER = LogFactory.getLog(WebSocketTest.class);

    private static final String SEND_CREATE_BOARD_ENDPOINT = "/app/create";
    private static final String SEND_MOVE_ENDPOINT = "/app/move/";
    private static final String SUBSCRIBE_CREATE_BOARD_ENDPOINT = "/topic/board";
    private static final String SUBSCRIBE_MOVE_ENDPOINT = "/topic/move/";

    private StompSession session;


    static final String WEBSOCKET_URI = "ws://localhost:8080/game";
    static final String WEBSOCKET_TOPIC = "/topic";

    BlockingQueue<String> blockingQueue;
    WebSocketStompClient stompClient;

    @Before
    public void setup() throws InterruptedException, ExecutionException, TimeoutException {
        blockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(
                asList(new WebSocketTransport(new StandardWebSocketClient()))));

        final List<Transport> transportList = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transportList));
        stompClient.setMessageConverter(new StringMessageConverter());
//        subscriptionCountDownLatch = new CountDownLatch(1);
//        subscriptionListener.registerCallback(e -> subscriptionCountDownLatch.countDown());

        session = stompClient.connect(WEBSOCKET_URI, new MySessionHandler()).get(5, SECONDS);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

//        subscriptionCountDownLatch.await(10, SECONDS);

        Thread.sleep(20);
    }

    @After
    public void reset() {
//        subscriptionListener.removeAllCallbacks();
        session.disconnect();
    }

//    @Test
//    public void shouldReceiveAMessageFromTheServer() throws Exception {
//        StompSession session = stompClient
//                .connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {
//                })
//                .get(1, SECONDS);
//        session.subscribe(WEBSOCKET_TOPIC, new DefaultStompFrameHandler());
//
//        String message = "MESSAGE TEST";
//        session.send(WEBSOCKET_TOPIC, message.getBytes());
//        String received = blockingQueue.poll(3, SECONDS);
//        LOGGER.info(received);
//
//        Assert.assertEquals(message, received);
//    }

    @Test
    public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {


//        session = stompClient.connect(WEBSOCKET_URI, new MySessionHandler()).get(5, SECONDS);
//


        session.subscribe(SUBSCRIBE_CREATE_BOARD_ENDPOINT, new DefaultStompFrameHandler());
        session.send(SEND_CREATE_BOARD_ENDPOINT, null);

        String received = blockingQueue.poll(3, SECONDS);
        LOGGER.info(received);
        assertNotNull(received);
    }

    class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            LOGGER.info(String.format("Handle Frame with payload: %s", o.toString()));
            try {
                blockingQueue.offer((String) o, 1, SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private class MySessionHandler extends StompSessionHandlerAdapter {
//        @Override
//        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//            session.subscribe(SUBSCRIBE_CREATE_BOARD_ENDPOINT, this);
//        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            LOGGER.warn("Stomp Error:", exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            super.handleTransportError(session, exception);
            LOGGER.warn("Stomp Transport Error:", exception);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            LOGGER.info(String.format("Handle Frame with payload: %s", o.toString()));
            try {
                blockingQueue.offer((String) o, 1, SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
