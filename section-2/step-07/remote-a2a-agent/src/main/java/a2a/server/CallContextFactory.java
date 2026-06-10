package a2a.server;

import jakarta.servlet.http.HttpServletRequest;

import io.a2a.server.ServerCallContext;

public interface CallContextFactory {
    ServerCallContext build(HttpServletRequest request);
}