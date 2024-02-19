package searchengine.dto.share;

import lombok.Data;

@Data
public class ErrorResponse extends DefaultResponse {
    private String error;

    public ErrorResponse() {
        super(false);
    }

    public static ErrorResponse getErrorMessage(String errorMessage){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(errorMessage);
        return errorResponse;
    }
}
