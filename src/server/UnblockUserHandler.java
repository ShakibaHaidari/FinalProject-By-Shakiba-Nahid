
package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.BlockedUserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UnblockUserHandler implements HttpHandler{
    private final BlockedUserService blockedUserService;
    public UnblockUserHandler(BlockedUserService blockedUserService){
        this.blockedUserService = blockedUserService;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.addCorsHeaders(exchange);
        if(HttpUtils.handleOptions(exchange)){
            return;
        }
        if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){
            HttpUtils.sendJson(exchange,
                    405,
                    """
                    {
                      "success": false,
                      "message": "Only POST method is allowed"
                    }
                    """
                  );
            return;
          }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = FormParser.parse(body);
        String userId = form.get("userId");
        String blockedUserId = form.get("blockedUserId");

        if(isBlank(userId) || isBlank(blockedUserId)){
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "userId and blockedUserId are required"
                    }
                    """
            );
            return;
        }

        boolean unblocked = blockedUserService.unblockUser(userId, blockedUserId);
        if(!unblocked){
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "User could not be unblocked"
                    }
                    """
            );
            return;
         }

        HttpUtils.sendJson(
                exchange,
                200,
                """
                {
                  "success": true,
                  "message": "User unblocked successfully"
                }
                """
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}