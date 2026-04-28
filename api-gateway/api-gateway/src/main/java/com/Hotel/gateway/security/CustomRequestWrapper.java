package com.Hotel.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class CustomRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> extraHeaders = new HashMap<>();

    public CustomRequestWrapper(HttpServletRequest request,
                                     String username,
                                     Long userId,
                                     String roles) {
        super(request);
        // Yeh headers downstream services ko milenge
        extraHeaders.put("X-User-Email", username);
        extraHeaders.put("X-User-Id", String.valueOf(userId));
        extraHeaders.put("X-User-Role", roles);
    }

    @Override
    public String getHeader(String name) {
        if (extraHeaders.containsKey(name)) {
            return extraHeaders.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (extraHeaders.containsKey(name)) {
            return Collections.enumeration(List.of(extraHeaders.get(name)));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(extraHeaders.keySet());
        return Collections.enumeration(names);
    }
}