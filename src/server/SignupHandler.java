package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class SignupHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String response = """
                {
                    "success": true,
                    "message": "Signup API Ready"
                }
                """;

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                200,
                response.getBytes().length
        );

        OutputStream os =
                exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
}