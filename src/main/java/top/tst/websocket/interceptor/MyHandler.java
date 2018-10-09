package top.tst.websocket.interceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class MyHandler implements WebSocketHandler {

	private Logger logger = LoggerFactory.getLogger(MyHandler.class);

	// 在线用户列表
	private static final Map<String, WebSocketSession> users;

	static {
		users = new HashMap<>();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.debug("=======================成功建立连接!!!!!!!!");
		String ID = session.getUri().toString().split("ID=")[1];
		logger.debug("sessionID--------------->{}", ID);
		if (ID != null) {
			users.put(ID, session);
			session.sendMessage(new TextMessage("成功建立socket连接"));
			logger.debug(ID);
			System.out.println("~~~~~~~~~~~~~~~~~~===>" + session);
		}
		System.out.println("当前在线人数：" + users.size());

	}

	// 接受Socket信息
	@Override
	public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage)
			throws Exception {
		try {

			logger.debug("[获取用户信息] ==================>{}", webSocketMessage.getPayload());
			logger.debug("=========================" + webSocketSession.getAttributes().get("WEBSKT_USRID") + "的消息");
			// JSONObject jsonobject = JSONObject.fromObject(webSocketMessage.getPayload());
			// System.out.println(jsonobject.get("id"));
			// System.out.println(jsonobject.get("message") + ":来自"
			// + (String) webSocketSession.getAttributes().get("WEBSOCKET_USERID") + "的消息");
			String userID = webSocketSession.getAttributes().get("WEBSKT_USRID").toString();
			logger.debug("~~~~~~~~~~~~~~~~~~~~~---->{}", userID);

			sendMessageToUser(userID, new TextMessage("服务器收到了吖吖吖吖吖吖吖吖，hello!"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** * 发送信息给指定用户 * @param clientId * @param message * @return */
	public boolean sendMessageToUser(String clientId, TextMessage message) {
		if (users.get(clientId) == null)
			return false;
		WebSocketSession session = users.get(clientId);
		System.out.println("sendMessage:" + session);
		if (!session.isOpen())
			return false;
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** * 广播信息 * @param message * @return */
	public boolean sendMessageToAllUsers(TextMessage message) {
		boolean allSendSuccess = true;
		Set<String> clientIds = users.keySet();
		WebSocketSession session = null;
		for (String clientId : clientIds) {
			try {
				session = users.get(clientId);
				if (session.isOpen()) {
					session.sendMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
				allSendSuccess = false;
			}
		}
		return allSendSuccess;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		System.out.println("连接出错");
		users.remove(getClientId(session));

	}

	/** * 获取用户标识 * @param session * @return */
	private Integer getClientId(WebSocketSession session) {
		try {
			Integer clientId = (Integer) session.getAttributes().get("WEBSKT_USRID");
			return clientId;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		String clientId = session.getAttributes().get("WEBSKT_USRID").toString();
		logger.debug("==========~~~~~~~~~~~~~~~~~==============>{}", clientId);
		users.remove(getClientId(session));
		logger.debug("关闭连接 移除session==============>>>>>>>>>>>{}", session.getId());
	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

}
