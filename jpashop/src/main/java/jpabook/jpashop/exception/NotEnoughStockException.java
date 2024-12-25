package jpabook.jpashop.exception;

// 아래에 있는 함수들은 보일러플레이트들
public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException() {
        super();
    }
    public NotEnoughStockException(String message) {
        super(message);
    }
    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }
    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
}
