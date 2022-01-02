package client;

public class ThreadBehavior {
    protected long rtt;
    protected int bestPort;
    protected long startByte;
    protected long endByte;
    protected int select_file_id;
    protected dummyClient.ObjectContainer packet;
    protected dummyClient inst = new dummyClient();

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
}
