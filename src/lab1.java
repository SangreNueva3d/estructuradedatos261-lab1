import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class lab1 {

    private static final String USER_FILE = "users.dat";
    private static final String INDEX_FILE = "index.dat";

    private static final int NUM_BUCKETS = 100;
    private static final long NULL_PTR = -1;

    private static final long NODE_SIZE = 8 + 8 + 8;

    private static final long BUCKET_AREA_SIZE = (long) NUM_BUCKETS * 8;


    public static void main(String[] args) throws IOException {
        resetFiles();
        initializeIndexFile();
        addUser(1111L, "pedro");
        addUser(1041L, "pepito");
        addUser(1121L, "pepita");

        searchWithoutIndex(1111L);
        searchWithIndex(1111L);

    }

    private static int hash(long cc){
        return (int) Math.abs(cc) % NUM_BUCKETS;
    }
    
    private static void resetFiles() throws IOException {
        Files.deleteIfExists(Path.of(USER_FILE));
        Files.deleteIfExists(Path.of(INDEX_FILE));
    }

    private static void initializeIndexFile() throws IOException{
        try(RandomAccessFile index = new RandomAccessFile(INDEX_FILE, "rw")){
            for(int i = 0; i < NUM_BUCKETS; i++){
                index.writeLong(NULL_PTR);    
            }
        }
    }

    private static void addUser(long cc, String name) throws IOException {

        long recordOffset;

         try(RandomAccessFile users = new RandomAccessFile(USER_FILE, "rw")){ 
            recordOffset = users.length();
            users.seek(recordOffset);

            users.writeLong(cc);
            users.writeUTF(name);
        }

        int bucket = hash(cc);
        
        try(RandomAccessFile index = new RandomAccessFile(INDEX_FILE, "rw")){ 
            long bucketPos = (long) bucket * 8;

            //Ir a la cabeza del bucket
            index.seek(bucketPos);
            long oldHead = index.readLong();

            //Agregamos el nodo al final

            long newNodeOffset = index.length();
            index.seek(newNodeOffset);

            index.writeLong(cc);
            index.writeLong(recordOffset);
            index.writeLong(oldHead);

            index.seek(bucketPos);
            index.writeLong(newNodeOffset);

            System.out.printf("Dato insertado: CC:%d| Nombre:%s |Bucket:%d |RecordOffset:%d |NodeOffset:%d%n| oldHead:%d",
                cc,name,bucket,recordOffset,newNodeOffset,oldHead
            );
        }

    }

    private static void searchWithoutIndex(long ccToFind)throws IOException{
        long startTime = System.nanoTime();
        int comparisons = 0;
        boolean found = false;
        long end = 0;

        try(RandomAccessFile users = new RandomAccessFile(USER_FILE, "r")){ 
            while(users.getFilePointer() < users.length()){
                long cc = users.readLong();
                String name = users.readUTF();
                comparisons++;

                if(cc==ccToFind){
                    found = true;
                    System.out.println("Encontrado usando indice");
                    System.out.println("CC: "+ cc);
                    System.out.println("Nombre: "+ name);
                    System.out.println("Comparaciones: "+ comparisons);
                    end = System.nanoTime();
                    printTime(startTime, end);
                    break;

                }
           
        }

    }

        if (!found){
            System.out.printf("No encontrado");
            System.out.printf("Comparaciones: "+ comparisons);
            end = System.nanoTime();
            printTime(startTime, end);
        }

    }

    private static void searchWithIndex(long ccToFind) throws IOException{

        long startTime = System.nanoTime();
        int comparisons = 0;

        int bucket = hash(ccToFind);
        
        try (RandomAccessFile index = new RandomAccessFile (INDEX_FILE,"rw");
        RandomAccessFile users = new RandomAccessFile (USER_FILE,"rw")) {

                long bucketPos = (long) bucket * 8;
                index.seek(bucketPos);

                long currentNodeOffset = index.readLong();

                while (currentNodeOffset != NULL_PTR){
                    index.seek(currentNodeOffset);

                    long cc = index.readLong();
                    long recordOffset = index.readLong();
                    long nextOffset = index.readLong();

                    comparisons ++;
                    
                    if (ccToFind ==cc){
                        users.seek(recordOffset);
                        long foundCc = users.readLong();
                        String foundName = users.readUTF();

                        System.out.println("Encontrado usando hash");
                        System.out.println("CC: "+ foundCc);
                        System.out.println("Nombre: "+ foundName);
                        System.out.println("Comparaciones: "+ comparisons);
                        long end = System.nanoTime();
                        printTime(startTime, end);
                        return;

                    }

                    currentNodeOffset = nextOffset;

                }

                System.out.printf("No encontrado");
                System.out.printf("Comparaciones: "+ comparisons);
                long end = System.nanoTime();
                printTime(startTime, end);

            

            }

    }

    private static void printTime(long start, long end){
        long nanos = end - start;
        long millis = nanos / 1_000_000;
        System.out.println("Tiempo" + nanos + "ns");
        System.out.println("Tiempo" + millis + "ms");
    }


    private static void printBucketUsed ()throws IOException{
        //TODO: IMPLEMENTAR

    }

    private static void printIndexNodes() throws IOException{
        //TODO: IMPLEMENTAR

    }
}
