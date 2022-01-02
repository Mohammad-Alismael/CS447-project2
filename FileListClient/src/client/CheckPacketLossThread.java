package client;

import java.io.IOException;

public class CheckPacketLossThread extends ThreadBehavior implements Runnable{
    protected dummyClient.ObjectContainer lastPacket;
    protected int packetsLost;
    protected boolean isItLastPacket;
    public dummyClient.ObjectContainer getLastPacket() {
        return lastPacket;
    }

    public void setLastPacket(dummyClient.ObjectContainer lastPacket) {
        this.lastPacket = lastPacket;
    }



    public int getPacketsLost() {
        return packetsLost;
    }

    public void setPacketsLost(int packetsLost) {
        this.packetsLost = packetsLost;
    }

    public boolean isItLastPacket() {
        return isItLastPacket;
    }

    public void setItLastPacket(boolean itLastPacket) {
        isItLastPacket = itLastPacket;
    }

    @Override
    public void run() {
        long packetEnding = 0;
        dummyClient.ObjectContainer packet = null;
        long timeout = 0;
        do {
            timeout = lastPacket.getTimeout();
            try {
                packet = inst.calculateRTTForDataPacker(
                        select_file_id, bestPort,
                        startByte,
                        endByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
            packetEnding = packet.getEnding();
            if (packetEnding >= timeout || packetEnding == 0){
                packetsLost++;

            }
            isItLastPacket = !(packetEnding >= timeout || packetEnding == 0);
        }
        while (packetEnding >= timeout || packetEnding == 0);

    }
}
