package zjy.android.zwebsocket;

import java.io.IOException;

import okio.Buffer;
import okio.ByteString;

public final class Contract {

    public static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /*
    Each frame starts with two bytes of data.

     0 1 2 3 4 5 6 7    0 1 2 3 4 5 6 7
    +-+-+-+-+-------+  +-+-------------+
    |F|R|R|R| OP    |  |M| LENGTH      |
    |I|S|S|S| CODE  |  |A|             |
    |N|V|V|V|       |  |S|             |
    | |1|2|3|       |  |K|             |
    +-+-+-+-+-------+  +-+-------------+
     */

    public static final int B0_FLAG_FIN = 128;

    public static final int B0_FLAG_RSV1 = 64;

    public static final int B0_FLAG_RSV2 = 32;

    public static final int B0_FLAG_RSV3 = 16;

    public static final int B0_OPCODE = 15;

    public static final int B1_FLAG_MASK = 128;

    public static final int B1_PAYLOAD_LENGTH = 127;

    public static final int OPCODE_FLAG_CONTROL = 8;

    /*
    opcode
     */

    public static final int OPCODE_CONTINUATION = 0x0;

    public static final int OPCODE_TEXT = 0x1;

    public static final int OPCODE_BINARY = 0x2;

    public static final int OPCODE_CLOSE = 0x8;

    public static final int OPCODE_PING = 0x9;

    public static final int OPCODE_PONG = 0xA;

    /*
    frame length
     */

    public static final long PAYLOAD_BYTE_MAX = 125L;

    public static final int PAYLOAD_SHORT = 126;

    public static final long PAYLOAD_SHORT_MAX = 0xffffL;

    public static final int PAYLOAD_LONG = 127;


    public static void toggleMask(byte[] data, int offset, int count, byte[] markKey) {
        int keyLen = markKey.length;
        for (int i = 0; i < count; i++, offset++) {
            int j = offset % keyLen;
            data[i] = (byte) (data[i] ^ markKey[j]);
        }
    }

    public static Buffer toggleMask(Buffer data, long offset, long count, byte[] markKey) throws IOException {
        int keyLen = markKey.length;
        Buffer markData = new Buffer();
        for (long i = 0; i < count; i++, offset++) {
            int j = (int) (offset % keyLen);
            int mark = data.readByte() ^ markKey[j];
            markData.writeByte(mark);
        }
        return markData;
    }

    public static Buffer toggleMask(ByteString data, int offset, int count, byte[] markKey) throws IOException {
        int keyLen = markKey.length;
        Buffer markData = new Buffer();
        for (int i = 0; i < count; i++, offset++) {
            int j = (int) (offset % keyLen);
            int mark = data.getByte(i) ^ markKey[j];
            markData.writeByte(mark);
        }
        return markData;
    }

}
