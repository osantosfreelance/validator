package open.osantos.validation.exception;

import javax.net.ssl.HttpsURLConnection;

public class ValidationException extends RuntimeException {

    private static final int code = HttpsURLConnection.HTTP_BAD_REQUEST;
    private String timeStamp = String.valueOf(System.currentTimeMillis());
    private String message;
    private String jsonNode;
    private String apiName;

    public ValidationException(String message, String jsonNode, String apiName) {
        this.message = message;
        this.jsonNode = jsonNode;
        this.apiName = apiName;
    }
}
