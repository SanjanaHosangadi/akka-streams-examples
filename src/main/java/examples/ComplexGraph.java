package examples;
import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.FlowShape;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.concurrent.CompletionStage;

public class ComplexGraph {

  public static void main(String[] args) {

    //Creating an actor system
    ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

    //source outputs integers from 1 to 10.
    Source<Integer, NotUsed> source = Source.range(1,10);

    //flow2 outputs square of every input integer
    Flow<Integer, Integer, NotUsed> flow2 =
        Flow.of(Integer.class).map(elem -> elem * 2);

    //flow3 multiplies every input integer by 3
    Flow<Integer, Integer, NotUsed> flow3 =
        Flow.of(Integer.class).map(elem -> elem * 3);

    //flow4 adds every input integer by 4
    Flow<Integer, Integer, NotUsed> flow4 =
        Flow.of(Integer.class).map(elem -> elem + 4);

    Flow<Integer, Integer, NotUsed> complex =
        Flow.fromGraph(
            GraphDSL.create(
                b -> {
                  //Broadcasts every input integer to 2 outlets
                  UniformFanOutShape<Integer, Integer> broadcast = b.add(Broadcast.create(2));
                  //Merges outputs of two outlets
                  UniformFanInShape<Integer, Integer> merge = b.add(Merge.create(2));
                  //Connecting the first outlet of the broadcast to flow2.
                  //Output of flow2 will be sent to the first inlet of merge
                  b.from(broadcast.out(0))
                      .via(b.add(flow2))
                      .toInlet(merge.in(0));
                  //Connecting the second outlet of the broadcast to flow3.
                  //Output of flow4 will be sent to the second inlet of merge
                  b.from(broadcast.out(1))
                      .via(b.add(flow3))
                      .via(b.add(flow4))
                      .toInlet(merge.in(1));

                  return FlowShape.of(broadcast.in(), merge.out());
                }));

    Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

    source
        .via(complex)
        .to(sink)
        .run(actorSystem);

  }

}

