
import java.lang.Thread;

public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread {
        public void run() {
            while (!ready) {
                Thread.yield();
            }
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        while( true) {
            new ReaderThread().start();
            number = 42;
            ready = true;
            try {
                // Thread.sleep(1000);
            }catch (Exception e){
                System.out.println(e);
            }
            
        }

    }
}