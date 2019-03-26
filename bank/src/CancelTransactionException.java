import java.lang.RuntimeException;
public class CancelTransactionException extends RuntimeException{
  CancelTransactionException(String message) {
    super(message);
  }
}
