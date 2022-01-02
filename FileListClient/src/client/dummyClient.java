package client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import model.FileDataResponseType;
import model.FileListResponseType;
import model.FileSizeResponseType;
import model.RequestType;
import model.ResponseType;
import model.ResponseType.RESPONSE_TYPES;

public class dummyClient {

	private void sendInvalidRequest(String ip, int port) throws IOException{
		 InetAddress IPAddress = InetAddress.getByName(ip); 
         RequestType req=new RequestType(4, 0, 0, 0, null);
         byte[] sendData = req.toByteArray();
         DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
         DatagramSocket dsocket = new DatagramSocket();
         dsocket.send(sendPacket);
         byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];
         DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
         dsocket.receive(receivePacket);
         ResponseType response=new ResponseType(receivePacket.getData());
         loggerManager.getInstance(this.getClass()).debug(response.toString());
	}
	
	private void getFileList(String ip, int port) throws IOException{
		InetAddress IPAddress = InetAddress.getByName(ip); 
        RequestType req=new RequestType(RequestType.REQUEST_TYPES.GET_FILE_LIST, 0, 0, 0, null);
        byte[] sendData = req.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(sendPacket);
        byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];
        DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
        dsocket.receive(receivePacket);
        FileListResponseType response=new FileListResponseType(receivePacket.getData());
        System.out.println(response);
        loggerManager.getInstance(this.getClass()).debug(response.toString());
	}
	
	private long getFileSize(String ip, int port, int file_id) throws IOException{
		InetAddress IPAddress = InetAddress.getByName(ip); 
        RequestType req=new RequestType(RequestType.REQUEST_TYPES.GET_FILE_SIZE, file_id, 0, 0, null);
        byte[] sendData = req.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(sendPacket);
        byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];
        DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
        dsocket.receive(receivePacket);
        FileSizeResponseType response = new FileSizeResponseType(receivePacket.getData());
        loggerManager.getInstance(this.getClass()).debug(response.toString());
        return response.getFileSize();
	}
	
	private void getFileData(String ip, int port, int file_id, long start, long end) throws IOException{
		InetAddress IPAddress = InetAddress.getByName(ip); 
        RequestType req=new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, file_id, start, end, null);
        byte[] sendData = req.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(sendPacket);
        byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];
        long maxReceivedByte=-1;
        while(maxReceivedByte<end){
        	DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
            dsocket.receive(receivePacket);
            FileDataResponseType response=new FileDataResponseType(receivePacket.getData());
            System.out.println(response);
            loggerManager.getInstance(this.getClass()).debug(response.toString());
            if (response.getResponseType() != RESPONSE_TYPES.GET_FILE_DATA_SUCCESS){
            	break;
            }
            if (response.getEnd_byte()>maxReceivedByte){
            	maxReceivedByte=response.getEnd_byte();
            };
        }
	}

    public FileDataResponseType getFileDataSpecific(String ip, int port, int file_id, long start, long end) throws IOException {
        InetAddress IPAddress = InetAddress.getByName(ip);
        RequestType req=new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, file_id, start, end, null);
        byte[] sendData = req.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(sendPacket);
        byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];

        DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
        dsocket.receive(receivePacket);
        FileDataResponseType response = new FileDataResponseType(receivePacket.getData());
//        System.out.println("reciveData array => "+ Arrays.toString(receiveData));
        return response;
    }
	public static dummyClient inst = new dummyClient();
	public static void main(String[] args) throws Exception{
		if (args.length<1){
			throw new IllegalArgumentException("ip:port is mandatory");
		}
        Scanner input = new Scanner(System.in);
		String[] adr1=args[0].split(":");
		String ip1=adr1[0];
		int port1 = Integer.valueOf(adr1[1]);
        int port2 = 5001;

		inst.sendInvalidRequest(ip1,port1);
		inst.getFileList(ip1,port1);
//        System.out.print("select valid file id> ");
//        int select_file_id = input.nextInt();
        int select_file_id = 4;
        System.out.println(String.format("File %d has been selected. Getting the size information...",select_file_id));
		long[] returnedValueFromPort1 = inst.calculateRTTForFileSize(select_file_id,port1);

        long[] returnedValueFromPort2 = inst.calculateRTTForFileSize(select_file_id,port2);
        System.out.println(String.format("File %d is %d bytes. Starting to download..",select_file_id,returnedValueFromPort1[0]));

        int howManyDataPackets = (int) (returnedValueFromPort1[0] / 990);
//        System.out.println("data packets =>"+ howManyDataPackets);

        int[] startByteEndByteAr = new int[howManyDataPackets];

        for (int i = 0; i < howManyDataPackets; i++) {
            startByteEndByteAr[i] = i * 990;
        }
//        System.out.println(Arrays.toString(startByteEndByteAr));

        int completed = 0;
        String string = "";


        int bestPort = 0;
        ObjectContainer packetTest1 = null;
        ObjectContainer packetTest2 = null;
        ObjectContainer lastPacket;
        long lastPort1Rtt = 0;
        long lastPort2Rtt = 0;
        Date d1 = new Date();
        long a = d1.getTime();
        int packetsLost = 0;
        for (int i = 0; i <howManyDataPackets - 1; i++) {

            if (i == 0) {
                 packetTest1 = inst.calculateRTTForDataPacker(
                        select_file_id, 5000,
                        startByteEndByteAr[i] + 1,
                        startByteEndByteAr[i + 1] + 1);
                packetTest2 = inst.calculateRTTForDataPacker(
                        select_file_id, 5001,
                        startByteEndByteAr[i] + 1,
                        startByteEndByteAr[i + 1] + 1);


                lastPort1Rtt = packetTest1.getRTT();
                lastPort2Rtt = packetTest2.getRTT();

            }
            System.out.println("lastPort1Rtt => "+ lastPort1Rtt);
            System.out.println("lastPort2Rtt => "+ lastPort2Rtt);
            if (lastPort1Rtt < lastPort2Rtt){
                bestPort = 5000;
                lastPacket = packetTest1;
            }else {
                bestPort = 5001;
                lastPacket = packetTest2;
            }

            System.out.println("best port =>"+ bestPort);

            // -----------------------------
            long packetEnding = 0;
            ObjectContainer packet;
            long timeout = 0;

//            do {
//                timeout = lastPacket.getTimeout();
//                packet = inst.calculateRTTForDataPacker(
//                        select_file_id, bestPort,
//                        startByteEndByteAr[i] + 1,
//                        startByteEndByteAr[i + 1] + 1);
//                packetEnding = packet.getEnding();
//                if (packetEnding >= timeout || packetEnding == 0){
//                    packetsLost++;
//                }
//            }
//            while (packetEnding >= timeout || packetEnding == 0);

            RequestingPacketThread requestingPacketThread = new RequestingPacketThread();
            requestingPacketThread.setSelect_file_id(select_file_id);
            requestingPacketThread.setStartByte(startByteEndByteAr[i] + 1);
            requestingPacketThread.setEndByte(startByteEndByteAr[i + 1] + 1);
            requestingPacketThread.setBestPort(bestPort);
            Thread thread2 = new Thread();
            thread2.start();

            CheckPacketLossThread checkPacketLossThread = new CheckPacketLossThread();
            checkPacketLossThread.setLastPacket(lastPacket);
            checkPacketLossThread.setSelect_file_id(select_file_id);
            checkPacketLossThread.setStartByte(startByteEndByteAr[i] + 1);
            checkPacketLossThread.setEndByte(startByteEndByteAr[i + 1] + 1);
            checkPacketLossThread.setBestPort(bestPort);
            checkPacketLossThread.setPacketsLost(packetsLost);
            // assigning the last packet sent
            if (checkPacketLossThread.isItLastPacket()){
                lastPacket = checkPacketLossThread.getPacket();
            }
            Thread thread1 = new Thread(checkPacketLossThread);
            thread1.start();

            long lastPacketRtt = lastPacket.getRTT();
            System.out.println("lastPacketRtt =>" + lastPacketRtt);
            if (lastPacket.getPort() == 5000){
                lastPort1Rtt = lastPacketRtt;
            }else {
                lastPort2Rtt = lastPacketRtt;
            }

            byte[] bytes = lastPacket.getPacketData();

            String comingStr = new String(bytes);
            string += comingStr;



        }

        Date d2 = new Date();
        long b = d2.getTime();

        long c = b -a ;
        System.out.println("time elapsed =>"+ c);


//        createFile();
        writeFile(string);

        // --------------- md5 sum ----------
        File file = new File("filename.txt");
        MessageDigest mdigest = MessageDigest.getInstance("MD5");
        System.out.println(md5sum(mdigest,file));
        // ----------------------------------

//        long RTT = 33;// ot can be considered as latency
//        double propagationTime = (double) RTT / 2;// it can be considered as propagation delay
//        double transferSpeed = (double) 1000 / (propagationTime *1000);
//        double bandwidth = 0;
//        System.out.println(String.format("RTT => %d ms",RTT));
//        System.out.println(String.format("approx propagation time => %f ms",propagationTime));
//        System.out.println(String.format("transfer speed => %f B/s",transferSpeed));
//        System.out.println(String.format("packet loss rate => %d ms",RTT));
//        System.out.println(String.format("percentage completed => %d ms",completed));
//        System.out.println(String.format("elapsed time => %d ms",RTT));

//		inst.getFileData(ip1,port1,select_file_id,1,900);
//		inst.getFileData(ip1,port1,1,30,20);
//		inst.getFileData(ip1,port1,1,1,fileSize);
	}


    public static void createFile(){
        try {
            File myObj = new File("filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeFile(String text){
        try {
            FileWriter myWriter = new FileWriter("filename.txt");
            myWriter.write(text);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static void setTimeout(Runnable runnable, long delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

    public static long[] calculateRTTForFileSize(int file_id, int port) throws IOException {
        Date date = new Date();
        long starting = date.getTime();
        long[] returnedValue = new long[2];
        returnedValue[0] = inst.getFileSize("127.0.0.1",port,file_id);
        Date date2 = new Date();
        long ending = date2.getTime();
        returnedValue[1] = ending - starting;
        return returnedValue;
    }

    public ObjectContainer calculateRTTForDataPacker(int file_id, int port,long startByte,long endByte) throws IOException {

        Date date = new Date();
        long starting = date.getTime();
        ObjectContainer objectContainer = new ObjectContainer();
        ResponseType responseType = inst.getFileDataSpecific("127.0.0.1",port,file_id,startByte,endByte);
        objectContainer.setResponseType(responseType);
        Date date2 = new Date();
        long ending = date2.getTime();
        objectContainer.setEnding(ending);
        // timeout = ending + (ending - starting)
        // timeout = 2 ending - starting
        objectContainer.setRTT(ending - starting);
        objectContainer.setPort(port);

        return objectContainer;
    }

    public static void calculateTransferSpeed(int file_id, int port){

    }

    public static void calculatePacketLossRate(){
        // sending 100 packets
        // checking how many packets received 85
        // return received / send * 100
    }

    public static String md5sum(MessageDigest digest, File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1)
        {
            digest.update(byteArray, 0, bytesCount);
        };

        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {

            sb.append(Integer
                    .toString((bytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }

        return sb.toString();
    }

    class ObjectContainer {
        long RTT;
        int port;
        byte[] packetData;
        long timeout;
        long ending = 0;
        ResponseType responseType;

        public void setRTT(long RTT) {
            this.RTT = RTT;
        }


        public long getRTT() {
            return RTT;
        }

        public byte[] getPacketData() {
            return packetData;
        }

        public ResponseType getResponseType() {
            return responseType;
        }

        public void setResponseType(ResponseType responseType) {
            this.responseType = responseType;
            this.packetData = responseType.getData();
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setEnding(long ending) {
            this.ending = ending;
        }

        public long getEnding() {
            return ending;
        }

        public long getTimeout() {
            timeout = ending + RTT;
            return timeout;
        }
    }
}
