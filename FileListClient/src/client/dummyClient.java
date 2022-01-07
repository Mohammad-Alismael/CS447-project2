package client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    public static DatagramSocket datagramSocket;
    public static int packetsLost = 0;
    public static int packetSent = 0;
    public static ArrayList<DatagramPacket> packetsForPort1 = new ArrayList();
    public static ArrayList<DatagramPacket> packetsForPort2 = new ArrayList();
    public static ArrayList<DatagramPacket> packetsLostForPort1 = new ArrayList();
    public static ArrayList<DatagramPacket> packetsLostForPort2 = new ArrayList();
    static {
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

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
        datagramSocket.send(sendPacket);
        byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];
        DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
        datagramSocket.receive(receivePacket);
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
//        DatagramSocket dsocket = new DatagramSocket();

        byte[] receiveData=new byte[ResponseType.MAX_RESPONSE_SIZE];

        DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);
        if (port == 5000){
            packetsForPort1.add(sendPacket);
        }else {
            packetsForPort2.add(sendPacket);
        }
        while (true) {
            try {
                datagramSocket.send(sendPacket);
                datagramSocket.receive(receivePacket);
                packetSent++;
                break;
            } catch (Exception e) {
                System.out.println("packet lost!");
                packetsLost++;
                if (port == 5000){
                    packetsLostForPort1.add(sendPacket);
                }else {
                    packetsLostForPort2.add(sendPacket);
                }
                continue;
            }
        }

        FileDataResponseType response = new FileDataResponseType(receivePacket.getData());
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
        System.out.println("ip=> "+ ip1);
        System.out.println("port=> "+ port1);
		inst.sendInvalidRequest(ip1,port1);
		inst.getFileList(ip1,port1);

        System.out.print("select valid file id> ");
        int select_file_id = input.nextInt();

		long[] returnedValueFromPort1 = inst.calculateRTTForFileSize(select_file_id,port1);
        while (returnedValueFromPort1[0] == -1){
            System.out.println("select a valid id...");
            System.out.print("select valid file id> ");
            select_file_id = input.nextInt();
            returnedValueFromPort1 = inst.calculateRTTForFileSize(select_file_id,port1);
        }
        System.out.println(String.format("File %d has been selected. Getting the size information...",select_file_id));

        System.out.println(String.format("File %d is %d bytes. Starting to download..",select_file_id,returnedValueFromPort1[0]));

        int howManyDataPackets = (int) Math.ceil((double) returnedValueFromPort1[0] / 990);
        System.out.println("data packets =>"+ howManyDataPackets);

        int[] startByteEndByteAr = new int[howManyDataPackets];

        for (int i = 0; i < howManyDataPackets; i++) {
            startByteEndByteAr[i] = i * 990;
        }

        int bestPort = 0;
        ObjectContainer packetTest1 = null;
        ObjectContainer packetTest2 = null;
        ObjectContainer lastPacket;
        ObjectContainer packet;
        long lastPort1Rtt = 0;
        long lastPort2Rtt = 0;
        long lastPacketRtt = 0;
        Date d1 = new Date();
        long a = d1.getTime();
        long elapsedTime = 0;
        createFile();
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
            if (lastPort1Rtt < lastPort2Rtt){
                bestPort = 5000;
                lastPacket = packetTest1;
            }else {
                bestPort = 5001;
                lastPacket = packetTest2;
            }
            double speed = 0;
            if (lastPacket.getRTT() == 0){
                speed = 100;
            }else {
                speed = (double) 2 / lastPacket.getRTT();
            }
            double lossRatePort1 = 0;
            if (packetsForPort1.size() == 0){
                lossRatePort1 = 0;
            }else {
                lossRatePort1 = ((double) packetsLostForPort1.size() / packetsForPort1.size());
            }
            double lossRatePort2 = 0;
            if (packetsForPort2.size() == 0){
                lossRatePort2 = 0;
            }else {
                lossRatePort2 = ((double) packetsLostForPort2.size() / packetsForPort2.size()) ;
            }
            System.out.println("----------------- stats -----------------------------");
            System.out.println(String.format("percentage of packet sent=> %f", ((double) dummyClient.packetSent/howManyDataPackets) * 100));
            System.out.println(String.format("transfer speed=> %f B/ms", speed));
            elapsedTime += lastPacket.getRTT();
            System.out.println(String.format("elapsed time=> %d ms", elapsedTime));
            System.out.println("loss rate for port 5000 => %" + (lossRatePort1 * 100));
            System.out.println("loss rate for port 5001 => %" + (lossRatePort2 * 100));
            System.out.println("packets for 5000=>"+ packetsForPort1.size());
            System.out.println("packets for 5001=>"+ packetsForPort2.size());
            System.out.println("packets loss for 5000=>"+ packetsLostForPort1.size());
            System.out.println("packets loss for 5001=>"+ packetsLostForPort2.size());
//            System.out.println("lastPort1Rtt => "+ lastPort1Rtt);
//            System.out.println("lastPort2Rtt => "+ lastPort2Rtt);
//            System.out.println("best port =>"+ bestPort);
            System.out.println("-----------------------------------------------------");
            packet = inst.calculateRTTForDataPacker(
                    select_file_id, bestPort,
                    startByteEndByteAr[i] + 1,
                    startByteEndByteAr[i + 1] + 1);
            dummyClient.datagramSocket.setSoTimeout((int) lastPacketRtt);

            lastPacketRtt = packet.getRTT();
//            System.out.println("lastPacketRtt =>" + lastPacketRtt);
            if (packet.getPort() == 5000){
                lastPort1Rtt = lastPacketRtt;
            }else {
                lastPort2Rtt = lastPacketRtt;
            }
            byte[] bytes = packet.getPacketData();
            String comingStr = new String(bytes);
            appendFile(comingStr);
        }

        Date d2 = new Date();
        long b = d2.getTime();

        long c = b - a;
        System.out.println("packet losses=>"+dummyClient.packetsLost);
        System.out.println("time elapsed =>"+ c);

        // --------------- md5 sum ----------
        File file = new File("filename.txt");
        MessageDigest mdigest = MessageDigest.getInstance("MD5");
        System.out.println(md5sum(mdigest,file));
//        printFileSizeNIO("filename.txt");
//        printFileSizeNIO("FileListServer/files/5MB.txt");
//        System.out.println("expected number of packets=> " + howManyDataPackets);
//        System.out.println("received number of packets=>=>" +
//                (packetsForPort1.size() + packetsForPort2.size()));
        System.out.println("Successfully wrote to the file.");
        // ----------------------------------
	}


    public static void createFile(){
        File myObj = new File("filename.txt");
        if (!myObj.exists()) {
            try {
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }else {
            writeFile("");
        }
    }

    public static void writeFile(String text){
        try {
            FileWriter myWriter = new FileWriter("filename.txt");
            myWriter.write(text);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    public static void appendFile(String text){
        try {
            Files.write(Paths.get("filename.txt"), text.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            System.out.println("error happened!");
        }
    }
    public static void printFileSizeNIO(String fileName) {
        Path path = Paths.get(fileName);
        try {
            long bytes = Files.size(path);
            System.out.println(String.format("%,d bytes", bytes));
            System.out.println(String.format("%,d kilobytes", bytes / 1024));
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        Instant start = Instant.now();
        ObjectContainer objectContainer = new ObjectContainer();
        ResponseType responseType = inst.getFileDataSpecific("127.0.0.1",port,file_id,startByte,endByte);
        objectContainer.setResponseType(responseType);
        Instant end = Instant.now();
        objectContainer.setEnding(end.toEpochMilli());
        // timeout = ending + (ending - starting)
        // timeout = 2 ending - starting
        Duration timeElapsed = Duration.between(start, end);
        objectContainer.setTimeout(2*end.toEpochMilli() - start.toEpochMilli());
        objectContainer.setRTT(timeElapsed.toMillis());
        objectContainer.setPort(port);

        return objectContainer;
    }


    public static String md5sum(MessageDigest digest, File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1)
        {
            digest.update(byteArray, 0, bytesCount);
        }

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
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
}
