package au.com.visiondigital.weatherDataBackend.exeption;



public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String city) {
        super("City not found: " + city);
    }
}