package top.tst.websocket.interceptor;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class WebSocketInterceptor implements HandshakeInterceptor {

	private Logger logger = LoggerFactory.getLogger(WebSocketInterceptor.class);

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
			Exception arg3) {
		System.out.println("====================>进入webSocket的afterHandShake拦截器");

	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
			Map<String, Object> map) throws Exception {

		if (request instanceof ServletServerHttpRequest) {
			String ID = request.getURI().toString().split("ID=")[1];
			logger.debug("当前session的ID===========>{}", ID);
			logger.debug("当前session的ID===========>{}", ID);
			logger.debug("当前session的ID===========>{}", ID);
			ServletServerHttpRequest httpRequest = (ServletServerHttpRequest) request;
			HttpSession session = httpRequest.getServletRequest().getSession();
			logger.debug("getSession的ID==========>{}", session.getId());
			logger.debug("getSession的ID==========>{}", session.getId());
			logger.debug("getSession的ID==========>{}", session.getId());
			map.put("WEBSKT_USRID", ID);
		}

		return true;
	}

}
