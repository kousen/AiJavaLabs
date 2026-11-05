package com.kousenit.demos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Condition for {@code @EnabledIf}: is LM Studio listening on its default port? */
final class LMStudioAvailable {
    private LMStudioAvailable() {}

    static boolean isRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 1234), 500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
