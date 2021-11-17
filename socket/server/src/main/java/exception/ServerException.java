package exception;

import java.io.IOException;

public class ServerException extends RuntimeException {
    public ServerException(String msg, IOException cause) {
        super(msg, cause);
    }
}
