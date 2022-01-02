package client;

import java.io.IOException;

public class RunnableImpl implements Runnable{
    private long rtt;
    private int bestPort;
    private long startByte;
    private long endByte;
    private int select_file_id;
    private dummyClient.ObjectContainer packet;
    public dummyClient inst;

    public long getRtt() {
        return rtt;
    }

    public void setRtt(long rtt) {
        this.rtt = rtt;
    }

    public int getBestPort() {
        return bestPort;
    }

    public void setBestPort(int bestPort) {
        this.bestPort = bestPort;
    }

    public long getStartByte() {
        return startByte;
    }

    public void setStartByte(long startByte) {
        this.startByte = startByte;
    }

    public long getEndByte() {
        return endByte;
    }

    public void setEndByte(long endByte) {
        this.endByte = endByte;
    }


    public void setInst(dummyClient inst) {
        this.inst = inst;
    }

    public int getSelect_file_id() {
        return select_file_id;
    }

    public void setSelect_file_id(int select_file_id) {
        this.select_file_id = select_file_id;
    }

    public dummyClient.ObjectContainer getPacket() {
        return packet;
    }

    public void setPacket(dummyClient.ObjectContainer packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            this.packet = inst.calculateRTTForDataPacker(
                    select_file_id, bestPort,
                    startByte,
                    endByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
