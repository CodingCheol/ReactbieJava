package begin;


import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class Ob {

    //Iterable  <--> Observer (Duality : 쌍극성)
    // 과거 Observer 패턴의 경우는 2가지 문제점으로 기피의 대상이 되었다.
    // 1. Complete 에 대한 결과값을 어떻게 할 것인가?
    // 2. Error 에 대한 처리는 어떻게 할 것인가?
    // 위의 2가지 의 상황을 극복하여 Reactive Java 의 Reactive Stream 이 탄생 하였다.
    static class IntObserver extends Observable implements Runnable{

        @Override
        public void run() {
            for( int i = 1; i <= 10 ; i++){
                setChanged();
                notifyObservers(i);     // push
            }
        }
    }

    public static void main(String[] args) {
        //Update method
        Observer observer = (Observable o, Object arg) ->{
                System.out.println(Thread.currentThread().getName() + " : "+ arg);
        };
        IntObserver ob = new IntObserver();
        ob.addObserver(observer);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        System.out.println(Thread.currentThread().getName() + " EXIT");

        executorService.execute(ob);

    }
}
