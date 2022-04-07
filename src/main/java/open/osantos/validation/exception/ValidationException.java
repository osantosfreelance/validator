package open.osantos.validation.exception;

import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;

@Getter
public class ValidationException extends RuntimeException {

    private static final int code = HttpsURLConnection.HTTP_BAD_REQUEST;
    private String timeStamp = String.valueOf(System.currentTimeMillis());
    private String message;
    private String jsonNode;
    private String apiName;

    public ValidationException(String apiName, String message, String jsonNode) {
        this.apiName = apiName;
        this.message = message;
        this.jsonNode = jsonNode;
    }
}
