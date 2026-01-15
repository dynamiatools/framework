package mybookstore;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import tools.dynamia.zk.websocket.ZKWebSocketConfigurer;

@Configuration
@EnableWebSocket
public class DemoWebSocketConfigurer extends ZKWebSocketConfigurer {

    //use default configuration
}
