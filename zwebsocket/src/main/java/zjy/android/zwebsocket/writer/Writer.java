package zjy.android.zwebsocket.writer;

import static zjy.android.zwebsocket.Contract.B0_FLAG_FIN;
import static zjy.android.zwebsocket.Contract.B1_FLAG_MASK;
import static zjy.android.zwebsocket.Contract.OPCODE_BINARY;
import static zjy.android.zwebsocket.Contract.OPCODE_CLOSE;
import static zjy.android.zwebsocket.Contract.OPCODE_PING;
import static zjy.android.zwebsocket.Contract.OPCODE_PONG;
import static zjy.android.zwebsocket.Contract.OPCODE_TEXT;
import static zjy.android.zwebsocket.Contract.PAYLOAD_BYTE_MAX;
import static zjy.android.zwebsocket.Contract.PAYLOAD_LONG;
import static zjy.android.zwebsocket.Contract.PAYLOAD_SHORT;
import static zjy.android.zwebsocket.Contract.PAYLOAD_SHORT_MAX;
import static zjy.android.zwebsocket.Contract.toggleMask;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import okio.Buffer;
import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;

public class Writer {

    private final BufferedSink sink;
    private final Buffer messageBuffer = new Buffer();

    private final byte[] markKey = new byte[4];
    private final Random random = new Random();

    private boolean isClosed = false;

    public Writer(OutputStream outputStream) {
        this.sink = Okio.buffer(Okio.sink(outputStream));
    }

    public void writePingFrame(ByteString payload) throws IOException {
        writeControlFrame(OPCODE_PING, payload);
    }

    public void writePongFrame(ByteString payload) throws IOException {
        writeControlFrame(OPCODE_PONG, payload);
    }

    public void writeCloseFrame(int code, ByteString reason) throws IOException {
        if (code == 0 && reason.size() == 0) {
            writeControlFrame(OPCODE_CLOSE, reason);
        } else {
            messageBuffer.writeShort(code);
            messageBuffer.write(reason);
            writeControlFrame(OPCODE_CLOSE, messageBuffer.readByteString());
            messageBuffer.clear();
        }
    }

    public synchronized void writeControlFrame(int opcode, ByteString payload) throws IOException {
        if (isClosed) throw new IOException("writer closed");
        if (payload.size() > 125) throw new IOException("control frame len is must be less 125B");

        int b0 = opcode | B0_FLAG_FIN;// opcode | 10000000
        sink.writeByte(b0);

        int b1 = payload.size() | B1_FLAG_MASK;// len | 10000000
        sink.writeByte(b1);

        random.nextBytes(markKey);
        sink.write(markKey);

        if (payload.size() > 0) {
            messageBuffer.write(payload);
            Buffer markData = toggleMask(messageBuffer, 0, payload.size(), markKey);
            sink.write(markData, payload.size());
        }
        sink.flush();
    }

    public void writeMessageFrame(String data) throws IOException {
        writeMessageFrame(OPCODE_TEXT, new ByteString(data.getBytes(StandardCharsets.UTF_8)));
    }

    public void writeMessageFrame(ByteString data) throws IOException {
        writeMessageFrame(OPCODE_BINARY, data);
    }

    public synchronized void writeMessageFrame(int opcode, ByteString data) throws IOException {
        if (isClosed) throw new IOException("writer closed");

        int b0 = opcode | B0_FLAG_FIN;// opcode | 10000000
        sink.writeByte(b0);

        messageBuffer.write(data);
        int b1 = B1_FLAG_MASK;
        long len = messageBuffer.size();
        if (len <= PAYLOAD_BYTE_MAX) {
            b1 |= len;
            sink.writeByte(b1);
        } else if (len <= PAYLOAD_SHORT_MAX) {
            b1 |= PAYLOAD_SHORT;
            sink.writeByte(b1);
            sink.writeShort((int) len);
        } else {
            b1 |= PAYLOAD_LONG;
            sink.writeByte(b1);
            sink.writeLong(len);
        }

        random.nextBytes(markKey);
        sink.write(markKey);

        if (len > 0) {
            Buffer markData = toggleMask(messageBuffer, 0, len, markKey);
            messageBuffer.clear();
            sink.write(markData, len);
        }

        sink.flush();
    }

    public synchronized void close() throws IOException {
        sink.close();
        isClosed = true;
    }

}
