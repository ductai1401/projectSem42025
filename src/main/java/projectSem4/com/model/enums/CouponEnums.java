package projectSem4.com.model.enums;

public class CouponEnums {
    public enum CouponType { PLATFORM, SHOP, FREESHIP }            // Toàn sàn / Theo shop
    public enum DiscountType { PERCENT, AMOUNT }         // % hoặc số tiền
    public enum Status {
        INACTIVE(0), ACTIVE(1), EXPIRED(2);
        private final int v;
        Status(int v){ this.v = v; }
        public int get(){ return v; }
        public static Status fromInt(int v){
            for (var s : values()) if (s.v == v) return s;
            return INACTIVE;
        }
    }
}
