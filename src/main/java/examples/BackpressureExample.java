package examples;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.concurrent.CompletionStage;

public class BackpressureExample {

  public static void main(String[] args) {

    //Creating an actor system
    ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

    //A Source that will send integers from 1 to 10.
    Source<Integer, NotUsed> source = Source.range(1, 20);

    //sqareFlow outputs square of every input integer
    Flow<Integer, Integer, NotUsed> flow1 = Flow.of(Integer.class).map(value -> {
      System.out.println("Flow 1 - " + value);
      return value * value;
    });

    Flow<Integer, Integer, NotUsed> flow2 = Flow.of(Integer.class).map(value -> {
      Thread.sleep(2000);
      return value;
    });

    //integerToStringFlow outputs String value of input integer
    Flow<Integer, String, NotUsed> flow3 = Flow.of(Integer.class).map(value -> {
      System.out.println("Flow 3 - " + value);
      return value.toString();
    });

    //sink prints input String
    Sink<String, CompletionStage<Done>> sink = Sink.ignore();


    source
        .via(flow1.buffer(1, OverflowStrategy.backpressure()))
        .async()
        .via(flow2)
        .via(flow3)
        .to(sink)
        .run(actorSystem);
  }

}
