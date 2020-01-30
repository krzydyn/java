package httpd;

public enum Status {
	OK(200, "OK"),

	// Client Error 4xx
	BAD_REQUEST(400, "Bad Request"),
	PAYMENT_REQUIRED(401, "Payment Required"),
	FORBIDDEN(403, "Forbidden"),
	FILE_NOT_FOUND(404, "File not found"),
	METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	NOT_ACCEPTABLE(406, "Not Acceptable"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	CONFLICT(409, "Conflict"),
	GONE(410, "Gone"),
	LENGTH_REQUIRED(411, "Length Required"),
	PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
	URI_TOO_LONG(414, "URI Too Long"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	EXPECTATION_FAILED(417, "Expectation Failed"),
	UPGRADE_REQUIRED(426, "Upgrade Required"),
	;

	public int getCode() { return rc; }
	public String getText() { return txt; }

	@Override
	public String toString() {
		return String.format("%d %s", rc, txt);
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
		return BAD_REQUEST;
	}
}
