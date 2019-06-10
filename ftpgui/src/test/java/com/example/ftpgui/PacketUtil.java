package com.example.ftpgui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

class PacketUtil {
    static byte[] constructPacket(Consumer<DataOutputStream> streamWriter) {
        var packetBody = new ByteArrayOutputStream();
        var packetBodyData = new DataOutputStream(packetBody);

        streamWriter.accept(packetBodyData);

        var packet = new ByteArrayOutputStream();
        var packetData = new DataOutputStream(packet);

        try {
            packetData.writeInt(packetBody.size());
            packetData.write(packetBody.toByteArray());
        } catch (IOException ignored) {
        }

        return packet.toByteArray();
    }
}
