package top.tst.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import top.tst.websocket.interceptor.MyHandler;
import top.tst.websocket.interceptor.WebSocketInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketH5Config implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// handler是webSocket的核心，配置入口
		registry.addHandler(new MyHandler(), "*/myHandler/{ID}").setAllowedOrigins("*")
				.addInterceptors(new WebSocketInterceptor());
	}

}
