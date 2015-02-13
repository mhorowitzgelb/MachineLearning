import weka.core.Attribute;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class NonClassNode extends Node {
    List<ConditionalProbability> conditionalProbabilities;
    List<Node> parents;
    List<Node> children;

    public NonClassNode(Attribute attribute){
        this.attribute = attribute;
        conditionalProbabilities = new ArrayList<ConditionalProbability>();
        parents = new ArrayList<Node>();
        children = new ArrayList<Node>();
    }


    @Override
    public double GetProbability(Instances instances) {
        return 0;
    }
}
