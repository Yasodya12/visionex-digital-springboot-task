package au.com.visiondigital.weatherDataBackend.exeption;


public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
