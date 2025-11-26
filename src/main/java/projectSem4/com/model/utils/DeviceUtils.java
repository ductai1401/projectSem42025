package projectSem4.com.model.utils;


public class DeviceUtils {
	private static final String[] MOBILE_KEYWORDS = { "Android", "webOS", "iPhone", "iPod", "BlackBerry", "IEMobile",
			"Opera Mini", "Mobile", "Windows Phone", "Kindle", "Silk" };
	private static final String[] TABLET_KEYWORDS = { "iPad", "Tablet", "Nexus 7", "Nexus 10", "SM-T" // có thể mở rộng
	};

	public static DeviceType detect(String userAgent) {
		if (userAgent == null)
			return DeviceType.DESKTOP;
		String ua = userAgent;
		for (String k : TABLET_KEYWORDS)
			if (ua.contains(k))
				return DeviceType.TABLET;
		for (String k : MOBILE_KEYWORDS)
			if (ua.contains(k))
				return DeviceType.MOBILE;
		return DeviceType.DESKTOP;
	}

	public static boolean isMobile(String ua) {
		return detect(ua) == DeviceType.MOBILE;
	}

	public static boolean isTablet(String ua) {
		return detect(ua) == DeviceType.TABLET;
	}

	public static boolean isDesktop(String ua) {
		return detect(ua) == DeviceType.DESKTOP;
	}
}
