package begin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;

// Reactive Stream
// 대용량 데이터 처리 시대에 맞춰 비동기 논블럭킹 함수형 자바 프로그래밍을 지향하기 위하여 대두 되었다.

//1. Publisher.subscribe(Subscriber) 호출
//2. Subscriber.onSubscribe 호출
//3. Subscriber.onNext 호출
//4. (optional) onError | onComplete 호출
public class PubSub {
    public static void main(String[] args) throws InterruptedException {
        Iterable<Integer> itr = Arrays.asList(1,2,3,4,5);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        //Publisher
        Publisher<Integer> publisher = new Publisher<Integer>() {
            Iterator<Integer> iterator = itr.iterator();
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                // Subscription 객체를 통하여 Publisher 와 Subscriber 는 서로 데이터 처리를 한다.
                // Why?
                //   비동기 논블럭킹 처리시 다음의 상황을 생각해보자.
                //   1. Publisher 의 요청 처리수가 많고, Subscriber 요청이 들어오는 처리를 하는데 버거운 경우.
                //   2. Publisher 의 요청 처리수가 적고, Subscriber 요청이 들어오는 처리를 하는데 빠른 경우.
                // 위 상황 이외의 여러 상황이 존재 할 수 있다. 이렇게 여러 상황의 경우에서 일정량의 데이터를 처리 하는데 유지 하게끔 Controller 하기 위하여
                // 사용되는 것이 Subscriber 객체이다. 이렇게 데이터에 대한 요청 처리를 Control 하는것은 BackPressure 라고 한다.
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        executor.execute(()-> {
                            int i = 0;
                            try {
                                //요청이 들어오는 개수 만큼만 처리
                                while (i++ < n){
                                    //데이터에 대한 요청을 전달한다라고 하면됨.
                                    if (iterator.hasNext()){
                                        subscriber.onNext(iterator.next());
                                    }
                                    else{
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            }catch (Exception e){
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
        //Subscriber -> Data sequential
        Subscriber<Integer> subscriber  = new Subscriber<>() {
            Subscription subscription;
            //2. Subscriber.onSubscribe
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " | OnSubscribe : " + subscription);
                this.subscription = subscription;
                this.subscription.request(2);
            }
            // 좀더 효율적으로 사용하기 위하여 스케쥴러를 사용하면된다.
            int bufferSize = 2;
            //3. Subscriber.onNext
            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " | OnNext : " + item);
                if( --bufferSize <= 0 ){
                    bufferSize = 2;
                    this.subscription.request(2);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + " | OnError : " + throwable );
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " | OnComplete");
            }
        };

        //1. Publisher.subscribe
        publisher.subscribe(subscriber);
        executor.awaitTermination(5, TimeUnit.SECONDS);
        executor.shutdown();

    }
}
