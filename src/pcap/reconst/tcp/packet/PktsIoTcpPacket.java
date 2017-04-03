package pcap.reconst.tcp.packet;

import io.pkts.packet.IPPacket;
import io.pkts.packet.TCPPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class PktsIoTcpPacket extends AbstractTcpPacket {
    public static final byte[] EMPTY_PAYLOAD = new byte[0];
    private InetAddress sourceIp = null;
    private InetAddress destinationIp = null;
    private TCPPacket tcpPacket;

    private int payloadLength;

    public PktsIoTcpPacket(TCPPacket tcpPacket) {
        this.tcpPacket = tcpPacket;

        try {
            this.sourceIp = InetAddress.getByAddress(
                    BigInteger.valueOf(tcpPacket.getRawSourceIp()).toByteArray());
            this.destinationIp = InetAddress.getByAddress(
                    BigInteger.valueOf(tcpPacket.getRawDestinationIp()).toByteArray());
        }
        catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }

        try {
            if (tcpPacket.getPacket(Protocol.ETHERNET_II) != null)
            {
                //Assume Ethernet/IP so 14 bytes for standard Ethernet header + IP length
                payloadLength = 14 + ((IPPacket) tcpPacket.getPacket(Protocol.IPv4)).getTotalIPLength();
            }
            else
            {
                payloadLength = (int) tcpPacket.getTotalLength();
            }
        } catch (IOException e) {
            //If this isn't a normal Ethernet/IP/TCP stack, then the best we can do is return the PCAP capture length.
            // This may be incorrect for padded ethernet frames.

            payloadLength = (int) tcpPacket.getTotalLength();
        }
    }

    @Override
    public InetAddress getSourceIP() {
        return sourceIp;
    }

    @Override
    public int getSourcePort() {
        return tcpPacket.getSourcePort();
    }

    @Override
    public InetAddress getDestinationIP() {
        return destinationIp;
    }

    @Override
    public int getDestinationPort() {
        return tcpPacket.getDestinationPort();
    }

    @Override
    public int getCaptureLength() {
        long caplen = tcpPacket.getCapturedLength();
        if (caplen == 0)
        {
            return (int) tcpPacket.getTotalLength();
        }
        else
        {
            return (int) caplen;
        }
    }

    @Override
    public int getLength() {
        return (int) tcpPacket.getTotalLength();
    }

    @Override
    public int getHeaderLength() {
        return payloadLength - getDataLength();
    }

    @Override
    public int getDataLength() {
        if (tcpPacket.getPayload() != null)
        {
            return tcpPacket.getPayload().capacity();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public long getSequence() {
        return tcpPacket.getSequenceNumber();
    }

    @Override
    public long getAckNum() { return tcpPacket.getAcknowledgementNumber(); }

    @Override
    public byte[] getData() {
        if (tcpPacket.getPayload() != null)
        {
            return tcpPacket.getPayload().getArray();
        }
        else
        {
            return EMPTY_PAYLOAD;
        }
    }

    @Override
    public boolean getSyn() {
        return tcpPacket.isSYN();
    }

    @Override
    public boolean getAck() {
        return tcpPacket.isACK();
    }

    @Override
    public boolean getFin() {
        return tcpPacket.isFIN();
    }

    @Override
    public boolean getPsh() {
        return tcpPacket.isPSH();
    }

    @Override
    public long getTimestampSec() {
        return TimeUnit.MICROSECONDS.toSeconds(tcpPacket.getArrivalTime());
    }

    @Override
    public long getTimestampUSec() {
        return tcpPacket.getArrivalTime() % 1000000;  //The last 6 digits
    }
}
