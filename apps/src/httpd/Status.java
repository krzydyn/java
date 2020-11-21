package httpd;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Status {
	public static final Status OK = new Status(200, "OK");

	public static final Status SEE_OTHER = new Status(303, "SEE OTHER");

	// Client Error 4xx
	public static final Status BAD_REQUEST = new Status(400, "Bad Request");
	public static final Status PAYMENT_REQUIRED = new Status(401, "Payment Required");
	public static final Status FORBIDDEN = new Status(403, "Forbidden");
	public static final Status FILE_NOT_FOUND = new Status(404, "File not found");
	public static final Status METHOD_NOT_ALLOWED = new Status(405, "Method Not Allowed");
	public static final Status NOT_ACCEPTABLE = new Status(406, "Not Acceptable");
	public static final Status REQUEST_TIMEOUT = new Status(408, "Request Timeout");
	public static final Status CONFLICT = new Status(409, "Conflict");
	public static final Status GONE = new Status(410, "Gone");
	public static final Status LENGTH_REQUIRED = new Status(411, "Length Required");
	public static final Status PAYLOAD_TOO_LARGE = new Status(413, "Payload Too Large");
	public static final Status URI_TOO_LONG = new Status(414, "URI Too Long");
	public static final Status UNSUPPORTED_MEDIA_TYPE = new Status(415, "Unsupported Media Type");
	public static final Status EXPECTATION_FAILED = new Status(417, "Expectation Failed");
	public static final Status UPGRADE_REQUIRED = new Status(426, "Upgrade Required");

	private static final Status values[];
	static {
		List<Status> sl = new ArrayList<>();
		Field[] declaredFields = Status.class.getDeclaredFields();

		for (Field field : declaredFields) {
			if (Modifier.isStatic(field.getModifiers())) {
				try {
					Object o = field.get(null);
					if (o instanceof Status)
						sl.add((Status)o);
				} catch (Exception e) {}
			}
		}
		values = sl.toArray(new Status[] {});
	}

	public int getCode() { return rc; }
	public String getText() { return txt; }

	@Override
	public String toString() {
		return String.format("%d %s", rc, txt);
	}

	public static Status[] values() {
		return values;
	}

	public String toString(int rc) {
		return String.format("%d %s", rc, "Unkonwn");
	}

	private Status(int rc, String txt) {
		this.rc = rc;
		this.txt = txt;
	}
	final private int rc;
	final private String txt;

	static Status getStatus(int rc) {
		for (Status s : Status.values()) {
			if (s.rc == rc) return s;
		}
		return new Status(rc, "<Unkown>");
	}
}
