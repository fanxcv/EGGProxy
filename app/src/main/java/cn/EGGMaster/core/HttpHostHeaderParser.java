package cn.EGGMaster.core;

import java.util.Locale;

import cn.EGGMaster.tcpip.CommonMethods;


class HttpHostHeaderParser {

    static String parseHost(byte[] buffer, int offset, int count) {
        try {
            switch (buffer[offset]) {
                case 'C'://CONNECT
                case 'G'://GET
                case 'P'://POST,PUT
                    return getHttpHost(buffer, offset, count);
                case 0x16://SSL
                    return getSNI(buffer, offset, count);
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    private static String getHttpHost(byte[] buffer, int offset, int count) {
        String headerString = new String(buffer, offset, count);
        String[] headerLines = headerString.split("\\r\\n", 2);
        if (headerLines[0].startsWith("GET") || headerLines[0].startsWith("POST")) {
            int i;
            if ((i = headerLines[1].toLowerCase(Locale.ENGLISH).indexOf("x-online-host")) >= 0) {
                return headerLines[1].substring(i + 14, headerLines[1].indexOf("\r\n", i)).trim();
            } else if ((i = headerLines[1].toLowerCase(Locale.ENGLISH).indexOf("host")) >= 0) {
                return headerLines[1].substring(i + 5, headerLines[1].indexOf("\r\n", i)).trim();
            }
        }
        return null;
    }

    private static String getSNI(byte[] buffer, int offset, int count) {
        int limit = offset + count;
        if (count > 43 && buffer[offset] == 0x16) {//TLS Client Hello
            offset += 43;//skip 43 bytes header

            //read sessionID:
            if (offset + 1 > limit) return null;
            int sessionIDLength = buffer[offset++] & 0xFF;
            offset += sessionIDLength;

            //read cipher suites:
            if (offset + 2 > limit) return null;
            int cipherSuitesLength = CommonMethods.readShort(buffer, offset) & 0xFFFF;
            offset += 2;
            offset += cipherSuitesLength;

            //read Compression method:
            if (offset + 1 > limit) return null;
            int compressionMethodLength = buffer[offset++] & 0xFF;
            offset += compressionMethodLength;

            if (offset == limit) {
                return null;
            }

            //read Extensions:
            if (offset + 2 > limit) return null;
            int extensionsLength = CommonMethods.readShort(buffer, offset) & 0xFFFF;
            offset += 2;

            if (offset + extensionsLength > limit) {
                //System.err.println("TLS Client Hello packet is incomplete.");
                return null;
            }

            while (offset + 4 <= limit) {
                int type0 = buffer[offset++] & 0xFF;
                int type1 = buffer[offset++] & 0xFF;
                int length = CommonMethods.readShort(buffer, offset) & 0xFFFF;
                offset += 2;

                if (type0 == 0x00 && type1 == 0x00 && length > 5) { //have SNI
                    offset += 5;//skip SNI header.
                    length -= 5;//SNI size;
                    if (offset + length > limit) return null;
                    return new String(buffer, offset, length);
                } else {
                    offset += length;
                }
            }
            return null;
        } else {
            return null;
        }
    }
}
