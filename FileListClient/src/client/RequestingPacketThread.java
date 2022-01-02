package client;

import java.io.IOException;

public class RequestingPacketThread extends ThreadBehavior implements Runnable{

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
