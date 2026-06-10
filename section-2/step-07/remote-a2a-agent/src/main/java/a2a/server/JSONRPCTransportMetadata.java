package a2a.server;

import io.a2a.server.TransportMetadata;
import io.a2a.spec.TransportProtocol;

public class JSONRPCTransportMetadata implements TransportMetadata {
    @Override
    public String getTransportProtocol() {
        return TransportProtocol.JSONRPC.asString();
    }
}