package za.co.eduvos.codebridge.core.network;
import java.io.IOException;
import retrofit2.HttpException;

// Domain Error base class
class DomainError extends Exception {
    public DomainError(String message) { super(message); }
    
    public static class NetworkError extends DomainError {
        public NetworkError() { super("No internet connection"); }
    }
    
    public static class UnauthorizedError extends DomainError {
        public UnauthorizedError() { super("Session expired. Please log in again."); }
    }
    
    public static class ApiError extends DomainError {
        private final String code;
        public ApiError(String code, String message) { 
            super(message); 
            this.code = code;
        }
        public String getCode() { return code; }
    }
    
    public static class UnknownError extends DomainError {
        public UnknownError(String message) { super(message); }
    }
}

public class ErrorParser {
    
    public static DomainError parse(Throwable throwable) {
        if (throwable instanceof IOException) {
            return new DomainError.NetworkError();
        } else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int code = httpException.code();
            
            if (code == 401) {
                return new DomainError.UnauthorizedError();
            } else {
                String errorBody = "";
                try {
                    if (httpException.response() != null && httpException.response().errorBody() != null) {
                        errorBody = httpException.response().errorBody().string();
                    }
                } catch (IOException e) {
                    // Ignore
                }
                return new DomainError.ApiError(String.valueOf(code), errorBody.isEmpty() ? "HTTP Error " + code : errorBody);
            }
        } else {
            return new DomainError.UnknownError(throwable.getMessage() != null ? throwable.getMessage() : "Unknown error");
        }
    }
}
