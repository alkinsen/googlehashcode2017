import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String input = "inputFile";
        BufferedReader br = new BufferedReader(new FileReader(input +".in"));

        String firstLine = br.readLine();
        String[] splitStr = firstLine.split("\\s+");

        int numVideos = Integer.parseInt(splitStr[0]);
        int numEndPoints = Integer.parseInt(splitStr[1]);
        int requestDescription = Integer.parseInt(splitStr[2]);
        int numCaches = Integer.parseInt(splitStr[3]);
        int sizeCaches = Integer.parseInt(splitStr[4]);

        System.out.println("v e r c x");
        System.out.println(firstLine);

        String secondLine = br.readLine();
        splitStr = secondLine.split("\\s+");
        int[] videoSizes = new int[splitStr.length];
        for (int i = 0; i < splitStr.length; i++) {
            videoSizes[i] = Integer.parseInt(splitStr[i]);
        }

        int[] endpointDatacenterLatency = new int[numEndPoints];
        int[][] endpointCacheConnectionGain = new int[numEndPoints][numCaches];

        for (int i = 0; i < numEndPoints; i++) {
            String endpointline = br.readLine();
            splitStr = endpointline.split("\\s+");
            int latency = Integer.parseInt(splitStr[0]);
            int cacheConn = Integer.parseInt(splitStr[1]);

            endpointDatacenterLatency[i] = latency;
            for (int j = 0; j < cacheConn; j++) {
                String cacheLine = br.readLine();
                splitStr = cacheLine.split("\\s+");
                int cacheIndex = Integer.parseInt(splitStr[0]);
                int cacheLatency = Integer.parseInt(splitStr[1]);
                if (cacheLatency == 0) System.out.println("latency is 0 change data structure");
                endpointCacheConnectionGain[i][cacheIndex] = latency - cacheLatency;
            }
        }

        ArrayList<HashMap<Integer, Integer>> requestInformation = new ArrayList<HashMap<Integer, Integer>>();
        for (int i = 0; i < numEndPoints; i++) {
            requestInformation.add(new HashMap<Integer, Integer>());
        }

        int[][] loadOfVideo = new int[numEndPoints][numVideos];

        for (int i = 0; i < requestDescription; i++) {
            String requestLine = br.readLine();
            splitStr = requestLine.split("\\s+");

            int videoNum = Integer.parseInt(splitStr[0]);
            int endpointNum = Integer.parseInt(splitStr[1]);
            int requestNum = Integer.parseInt(splitStr[2]);

            HashMap<Integer, Integer> currentHashmap = requestInformation.get(endpointNum);
            currentHashmap.put(videoNum, requestNum);

            loadOfVideo[endpointNum][videoNum] = requestNum * videoSizes[videoNum];
        }

        int[] totalLoad = new int[numEndPoints];
        for(int i = 0; i < numEndPoints; i++){
            int sum = 0;
            for(int j = 0; j < numVideos; j++) {
                sum += loadOfVideo[i][j];
            }
            totalLoad[i] = sum;
        }


        HashMap<Integer, List<Integer>> cachedVideos = new HashMap<Integer, List<Integer>>();

        for (int i = 0; i < numCaches; i++) {
            cachedVideos.put(i, new ArrayList<>());
        }



        for (int i = 0; i < numEndPoints; i++) {
            int videoIndex = findMax(i, loadOfVideo);
            double percentage = loadOfVideo[i][videoIndex] / totalLoad[i];
            ArrayList<Integer> cacheIndexes = findMaxCache(i, endpointCacheConnectionGain, videoSizes[videoIndex], videoSizes, cachedVideos, sizeCaches, percentage);
                for (int cacheIndex : cacheIndexes) {
                    if (videoIndex != -1) {
                        if (!cachedVideos.get(cacheIndex).contains(videoIndex))
                            cachedVideos.get(cacheIndex).add(videoIndex);
                        loadOfVideo[i][videoIndex] -= endpointCacheConnectionGain[i][cacheIndex] * videoSizes[videoIndex];
                        if (findMax(i, loadOfVideo) != videoIndex) break;
                    }
                }
            }


            int numusedCache = 0;
            for(Map.Entry<Integer, List<Integer>> entry : cachedVideos.entrySet()){
                if(entry.getValue().size() != 0){
                    numusedCache++;
                }
            }
            FileWriter fw = new FileWriter(new File(input+".out"));
            fw.write(numusedCache + "\n");
            for(Map.Entry<Integer, List<Integer>> entry : cachedVideos.entrySet()){

                if(entry.getValue().size() != 0){
                    String cache = ""+entry.getKey();
                    for(Integer value : entry.getValue()){
                        cache +=  " " + value ;
                    }
                    fw.write(cache+"\n");
                }
            }
            fw.close();
        }



    public static ArrayList<Integer> findMaxCache(int endpoint, int[][] endpointCacheGains, int required, int[] videoSizes, HashMap<Integer, List<Integer>> cachedVideos, int sizeCaches, double percentage) {
        //Calculating the gain for each video
        ArrayList<Integer> result = new ArrayList<>();
        int numCaches = endpointCacheGains[endpoint].length;

        for (int i = 0; i < endpointCacheGains[endpoint].length; i++) {
            for (int k = 0; k < numCaches; k++) {
                int sizeLeft = sizeLeft(k, videoSizes, cachedVideos, sizeCaches);
                if (required < sizeLeft) {
                    result.add(k);
                }
            }
        }


        return result;
    }

    public static int sizeLeft(int cache, int[] videoSizes, HashMap<Integer, List<Integer>> cachedVideos, int capacity) {
        int currentSize = 0;
        ArrayList<Integer> videos = (ArrayList<Integer>) cachedVideos.get(cache);
        for (Integer video : videos) {
            currentSize += videoSizes[video];
        }
        return capacity - currentSize;
    }

    public static int findMax(int endpoint, int[][] gainsLatency) {
        int max = 0;
        int maxIndex = -1;

        for (int i = 0; i < gainsLatency[endpoint].length; i++) {
            int currentGain = gainsLatency[endpoint][i];
            if ( currentGain > max) {
                max = currentGain;
                maxIndex = i;
            }
        }

        return maxIndex;
    }
}
