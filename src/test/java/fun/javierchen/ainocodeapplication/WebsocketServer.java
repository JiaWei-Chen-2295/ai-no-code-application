package fun.javierchen.ainocodeapplication;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.core.http.ServerWebSocket;
import org.junit.jupiter.api.Test;

//@SpringBootTest
public class WebsocketServer {
    @Test
    void runWebSocketServer() {
        Vertx vertx = Vertx.builder().build();
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(8081);
        HttpServer httpServer = vertx.createHttpServer(options)
                .webSocketHandler(new Handler<ServerWebSocket>() {
                    @Override
                    public void handle(ServerWebSocket serverWebSocket) {
                        serverWebSocket.frameHandler(new Handler<WebSocketFrame>() {
                            @Override
                            public void handle(WebSocketFrame webSocketFrame) {
                                if (webSocketFrame.isText()) {
                                    String s = webSocketFrame.textData();
                                    System.out.println("接收到消息： " + s);

                                }
                            }
                        });
                    }
                });
        Single<HttpServer> listen = httpServer.listen();
        listen.subscribe((server, throwable) -> {
            int i = server.actualPort();
            System.out.println("启动成功，端口： " + i);
        });


    }
}
