package com.ruoyi.biz.pay;

/**
 * Captures the latest PayHttp call on the current thread for gateway log persistence.
 */
public final class PayHttpExchange
{
    private static final ThreadLocal<Snapshot> HOLDER = new ThreadLocal<Snapshot>();

    private PayHttpExchange()
    {
    }

    public static void clear()
    {
        HOLDER.remove();
    }

    public static void record(String url, String requestBody, String responseBody, Integer httpStatus, long costMs)
    {
        Snapshot snap = new Snapshot();
        snap.url = url;
        snap.requestBody = requestBody;
        snap.responseBody = responseBody;
        snap.httpStatus = httpStatus;
        snap.costMs = Long.valueOf(costMs);
        HOLDER.set(snap);
    }

    public static Snapshot get()
    {
        return HOLDER.get();
    }

    public static final class Snapshot
    {
        private String url;
        private String requestBody;
        private String responseBody;
        private Integer httpStatus;
        private Long costMs;

        public String getUrl() { return url; }
        public String getRequestBody() { return requestBody; }
        public String getResponseBody() { return responseBody; }
        public Integer getHttpStatus() { return httpStatus; }
        public Long getCostMs() { return costMs; }
    }
}
