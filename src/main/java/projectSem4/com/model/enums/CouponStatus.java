package projectSem4.com.model.enums;

import projectSem4.com.model.enums.CouponEnums.Status;

public enum CouponStatus {
	INACTIVE(0),
	ACTIVE(1),
	EXPIRED(2);
    private final int v;
    
    CouponStatus(int v){ this.v = v; }
    public int get(){ return v; }
    public static CouponStatus fromInt(int v){
        for (var s : values()) if (s.v == v) return s;
        return INACTIVE;
    }
}
