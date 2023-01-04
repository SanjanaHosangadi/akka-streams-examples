package examples;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.concurrent.CompletionStage;

public class SimpleGraph {

  public static void main(String[] args) {

    //Creating an actor system
    ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

    //A Source that will send integers from 1 to 5.
    Source<Integer, NotUsed> source = Source.range(1, 5);

    //sqareFlow outputs square of every input integer
    Flow<Integer, Integer, NotUsed> squareFlow = Flow.of(Integer.class).map(value -> value * value);

    //integerToStringFlow outputs String value of input integer
    Flow<Integer, String, NotUsed> integerToStringFlow = Flow.of(Integer.class).map(value -> value.toString());

    //sink prints input String
    Sink<String, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

    source
        .via(squareFlow)
        .via(integerToStringFlow)
        .to(sink)
        .run(actorSystem);
  }

}

